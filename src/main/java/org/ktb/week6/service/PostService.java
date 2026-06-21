package org.ktb.week6.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.ktb.week6.dto.PostListResponseDto;
import org.ktb.week6.dto.PostRequestDto;
import org.ktb.week6.dto.PostResponseDto;
import org.ktb.week6.dto.PostUpdateRequestDto;
import org.ktb.week6.entity.*;
import org.ktb.week6.enums.ActionType;
import org.ktb.week6.enums.FileCategory;
import org.ktb.week6.enums.ReportReason;
import org.ktb.week6.enums.StatusType;
import org.ktb.week6.exception.BusinessException;
import org.ktb.week6.exception.NotFoundException;
import org.ktb.week6.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {
    private static final int POST_PAGE_SIZE = 10;

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final ReportRepository reportRepository;
    private final TemporaryPostService temporaryPostService;
    private final PostHistoryRepository postHistoryRepository;
    private final PostViewLogsRepository postViewLogsRepository;

    private final FileService fileService;

    // 게시글 목록 조회
    @Transactional(readOnly = true)
    public PostListResponseDto getPosts(Long cursorId) {
        Long currentCursorId = cursorId == null ? Long.MAX_VALUE : cursorId;
        List<Post> activePosts = postRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(post -> !post.getStatus().equals(StatusType.DELETED))
                .toList();

        long totalPosts = activePosts.size();
        List<Post> cursorPosts = activePosts.stream()
                .filter(post -> post.getId() < currentCursorId)
                .sorted(Comparator.comparing(Post::getId).reversed())
                .limit(POST_PAGE_SIZE + 1)
                .toList();

        boolean hasNext = cursorPosts.size() > POST_PAGE_SIZE;
        List<Post> pagePosts = hasNext ? cursorPosts.subList(0, POST_PAGE_SIZE) : cursorPosts;
        Long responseNextCursorId = hasNext ? pagePosts.getLast().getId() : null;
        List<PostResponseDto> posts = pagePosts.stream()
                .map(this::toPostResponseDto)
                .toList();

        return new PostListResponseDto(
                posts,
                new PostListResponseDto.Pagination(totalPosts, responseNextCursorId, hasNext)
        );
    }

    // 게시글 작성
    @Transactional
    public PostResponseDto createPost(@Positive Long userId, PostRequestDto request, MultipartFile image) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("user_not_found"));

        Optional<File> file = fileService.storeFile(image, FileCategory.POST_ATTACHMENT);

        Post post = new Post(request.getTitle(), request.getContent(), user, file.orElse(null));

        Post savedPost = postRepository.save(post);

        // 임시저장 이력도 함께 삭제
        if (request.getTemporaryPostId() != null) {
            temporaryPostService.deleteTemporaryPost(request.getTemporaryPostId(), userId);
        }

        Long nextVersion = postHistoryRepository.findMaxVersionByPostId(post.getId()) + 1;

        // 이력 저장
        PostHistory postHistory = new PostHistory(ActionType.INSERT, post.getTitle(), post.getContent(), nextVersion, post, user, file.orElse(null));

        postHistoryRepository.save(postHistory);

        return new PostResponseDto(savedPost);
    }

    // 게시글 상세 조회
    @Transactional
    public PostResponseDto getPost(Long postId, Long userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("post_not_found"));

        if (post.getStatus().equals(StatusType.DELETED)) {
            throw new NotFoundException("post_not_found");
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("user_not_found"));

        PostViewLogs viewLogs = postViewLogsRepository.findByPostIdAndUserId(postId, userId).orElse(null);
        // 24시간 내 중복 조회수 제한 - 아직 본 적 없을 때
        if(viewLogs == null) {
            PostViewLogs newViewLogs = new PostViewLogs(post, user);
            postViewLogsRepository.save(newViewLogs);

            post.increaseViewCount();
        } else if (viewLogs.isExpired()) {
            post.increaseViewCount();

            // 24시간 지나면 기존 조회 이력 삭제
            postViewLogsRepository.delete(viewLogs);
        }

        return new PostResponseDto(post);
    }

    // 게시글 수정
    @Transactional
    public PostResponseDto updatePost(@Positive Long userId, @Positive Long postId, @Valid PostUpdateRequestDto request, MultipartFile image) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("post_not_found"));
        validatePostIsNotDeleted(post);

        // 수정자 권한 체크
        if (!post.getUser().getId().equals(userId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "post_update_forbidden");
        }

        boolean hasTitle = request.getTitle() != null && !request.getTitle().isBlank();
        boolean hasContent = request.getContent() != null && !request.getContent().isBlank();
        boolean hasImage = image != null && !image.isEmpty();

        if (!hasTitle && !hasContent && !hasImage) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "title_content_required");
        }

        if (hasTitle) {
            post.updateTitle(request.getTitle());
        }

        if (hasContent) {
            post.updateContent(request.getContent());
        }

        if (hasImage) {
            Optional<File> file = fileService.storeFile(image, FileCategory.POST_ATTACHMENT);
            post.updateFile(file.orElse(null));
        }

        Long nextVersion = postHistoryRepository.findMaxVersionByPostId(post.getId()) + 1;

        // 이력 저장
        PostHistory postHistory = new PostHistory(ActionType.UPDATE, post.getTitle(), post.getContent(), nextVersion, post, post.getUser(), post.getFile());

        postHistoryRepository.save(postHistory);

        return new PostResponseDto(post);
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(@Positive Long userId, @Positive Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("post_not_found"));
        validatePostIsNotDeleted(post);

        // 삭제 권한 체크
        if (!post.getUser().getId().equals(userId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "post_delete_forbidden");
        }

        post.updateStatus(StatusType.DELETED);

        Long nextVersion = postHistoryRepository.findMaxVersionByPostId(post.getId()) + 1;

        // 이력 저장
        PostHistory postHistory = new PostHistory(ActionType.DELETE, post.getTitle(), post.getContent(), nextVersion, post, post.getUser(), post.getFile());

        postHistoryRepository.save(postHistory);
    }

    // 게시글 좋아요
    @Transactional
    public PostResponseDto likePost(@Positive Long userId, @Positive Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("post_not_found"));

        validatePostIsNotDeleted(post);

        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("user_not_found"));

        // 이미 좋아요 눌렀으면 제거
        likeRepository.findByPostAndUser(post, user)
                .ifPresentOrElse(
                        like -> {
                            likeRepository.delete(like);
                            post.decreaseLikeCount();
                        },
                        () -> {
                            likeRepository.save(new Like(post, user));
                            post.increaseLikeCount();
                        }
                );

        return new PostResponseDto(post);
    }

    // 게시글 신고
    @Transactional
    public PostResponseDto reportPost(@Positive Long userId, @Positive Long postId, ReportReason reason) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("post_not_found"));
        validatePostIsNotDeleted(post);
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("user_not_found"));

        // 중복 신고 방지
        if (reportRepository.findByPostIdAndUserId(postId, userId).isPresent()) {
            throw new BusinessException(HttpStatus.CONFLICT, "post_already_reported");
        }

        reportRepository.save(new Report(null, post, user, reason));
        post.increaseReportCount();

        // 신고 수 누적 체크
        if (post.getReportCount() >= 5) {
            post.updateStatus(StatusType.BLIND);
        }

        return new PostResponseDto(post);
    }

    private void validatePostIsNotDeleted(Post post) {
        if (post.getStatus().equals(StatusType.DELETED)) {
            throw new NotFoundException("post_not_found");
        }
    }

    private PostResponseDto toPostResponseDto(Post post) {
        return new PostResponseDto(post);
    }
}
