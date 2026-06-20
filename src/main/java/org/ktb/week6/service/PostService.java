package org.ktb.week6.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.ktb.week6.dto.PostListResponseDto;
import org.ktb.week6.dto.PostRequestDto;
import org.ktb.week6.dto.PostResponseDto;
import org.ktb.week6.dto.PostUpdateRequestDto;
import org.ktb.week6.entity.*;
import org.ktb.week6.enums.FileCategory;
import org.ktb.week6.enums.StatusType;
import org.ktb.week6.exception.BusinessException;
import org.ktb.week6.exception.NotFoundException;
import org.ktb.week6.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private static final int POST_PAGE_SIZE = 10;

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    private final FileService fileService;
    private final LikeRepository likeRepository;
    private final ReportRepository reportRepository;

    // 게시글 목록 조회 TODO: 숨겨진 게시글 처리
    public PostListResponseDto getPosts(Long cursorId) {
        Long currentCursorId = cursorId == null ? Long.MAX_VALUE : cursorId;
        List<Post> activePosts = postRepository.findAll().stream()
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
        Long responseNextCursorId = hasNext ? pagePosts.get(pagePosts.size() - 1).getId() : null;
        List<PostResponseDto> posts = pagePosts.stream()
                .map(this::toPostResponseDto)
                .toList();

        return new PostListResponseDto(
                posts,
                new PostListResponseDto.Pagination(totalPosts, responseNextCursorId, hasNext)
        );
    }

    // 게시글 작성
    public PostResponseDto createPost(@Positive Long userId, PostRequestDto request, MultipartFile image) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("user_not_found"));

        File file = fileService.storeFile(image, userId, FileCategory.POST_ATTACHMENT);

        Post post = new Post(request.getTitle(), request.getContent(), user, file);

        Post savedPost = postRepository.save(post);

        return new PostResponseDto(savedPost);
    }

    // 게시글 상세 조회
    public PostResponseDto getPost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("post_not_found"));
        if (post.getStatus().equals(StatusType.DELETED)) {
            throw new NotFoundException("post_not_found");
        }

        post.increaseViewCount();
        postRepository.save(post);

        return new PostResponseDto(post);
    }

    // 게시글 수정
    public PostResponseDto updatePost(@Positive Long userId, @Positive Long postId, @Valid PostUpdateRequestDto request, MultipartFile image) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("post_not_found"));
        validatePostIsNotDeleted(post);
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("user_not_found"));

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
            File file = fileService.storeFile(image, userId, FileCategory.POST_ATTACHMENT);
            post.updateFile(file);
        }

        postRepository.save(post);

        return new PostResponseDto(post);
    }

    // 게시글 삭제
    public void deletePost(@Positive Long userId, @Positive Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("post_not_found"));
        validatePostIsNotDeleted(post);

        // 삭제 권한 체크
        if (!post.getUser().getId().equals(userId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "post_delete_forbidden");
        }

        post.updateStatus(StatusType.DELETED);
        postRepository.save(post);
    }

    // 게시글 좋아요
    public PostResponseDto likePost(@Positive Long userId, @Positive Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("post_not_found"));
        validatePostIsNotDeleted(post);
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("user_not_found"));

        // 이미 좋아요 눌렀으면 제거
        likeRepository.findByPostIdAndUserId(postId, userId)
                .ifPresentOrElse(
                        like -> {
                            likeRepository.deleteById(like);
                            post.decreaseLikeCount();
                        },
                        () -> {
                            likeRepository.save(new Like(post, user));
                            post.increaseLikeCount();
                        }
                );

        postRepository.save(post);
        return new PostResponseDto(post);
    }

    // 게시글 신고
    public PostResponseDto reportPost(@Positive Long userId, @Positive Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("post_not_found"));
        validatePostIsNotDeleted(post);
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("user_not_found"));

        // 중복 신고 방지
        if (reportRepository.findByPostIdAndUserId(postId, userId).isPresent()) {
            throw new BusinessException(HttpStatus.CONFLICT, "post_already_reported");
        }

        reportRepository.save(new Report(null, postId, userId));
        post.increaseReportCount();

        // 신고 수 누적 체크
        if (post.getReportCount() >= 5) {
            post.updateStatus(StatusType.BLIND);
        }

        postRepository.save(post);

        return new PostResponseDto(post);
    }

    private void validatePostIsNotDeleted(Post post) {
        if (post.getStatus().equals(StatusType.DELETED)) {
            throw new NotFoundException("post_not_found");
        }
    }

    private PostResponseDto toPostResponseDto(Post post) {
        User user = userRepository.findById(post.getUser().getId())
                .orElseThrow(() -> new NotFoundException("user_not_found"));

        return new PostResponseDto(post);
    }
}
