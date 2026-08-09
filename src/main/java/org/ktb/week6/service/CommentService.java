package org.ktb.week6.service;

import lombok.RequiredArgsConstructor;
import org.ktb.week6.dto.CommentRequestDto;
import org.ktb.week6.dto.CommentResponseDto;
import org.ktb.week6.entity.*;
import org.ktb.week6.enums.StatusType;
import org.ktb.week6.exception.BusinessException;
import org.ktb.week6.exception.NotFoundException;
import org.ktb.week6.repository.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Validated
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final EventPostRepository eventPostRepository;
    private final EventApplicationRepository eventApplicationRepository;

    // 댓글 작성 (공통)
    @Transactional
    public CommentResponseDto createComment(CommentRequestDto request, Long postId, Long userId) {
        Post post = postRepository.findByStatusNotAndId(StatusType.DELETED, postId).orElseThrow(() -> new NotFoundException("post_not_found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("user_not_found"));

        Optional<EventPost> eventPost = eventPostRepository.existsByPostId(post.getId())
                ? eventPostRepository.findByPostIdForUpdate(post.getId())
                : Optional.empty();

        return eventPost.map(value -> createEventComment(request, post, user, value)).orElseGet(() -> createGeneralComment(request, post, user));
    }

    private CommentResponseDto createGeneralComment(CommentRequestDto request, Post post, User user) {
        Optional<Comment> parent = validateAndGetParentComment(request.getParentId(), post.getId());

        Comment comment = new Comment(
                request.getContent(),
                post,
                user,
                parent.orElse(null)
        );

        Comment savedComment = commentRepository.save(comment);

        postRepository.increaseCommentCount(post.getId());

        return new CommentResponseDto(savedComment);
    }

    private CommentResponseDto createEventComment(CommentRequestDto request, Post post, User user, EventPost eventPost) {

        if (post.getUser().getId().equals(user.getId())) {
            throw new BusinessException(
                    HttpStatus.FORBIDDEN,
                    "application_self_forbidden"
            );
        }

        if (eventApplicationRepository
                .existsByEventPostIdAndUserId(eventPost.getId(), user.getId())) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "application_already_exists"
            );
        }

        if (eventPost.isExpired()) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "application_expired"
            );
        }

        if (eventPost.isFull()) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "application_count_full"
            );
        }

        Comment newComment = new Comment(request.getContent(), post, user, null);
        EventApplication newEventApplication = new EventApplication(eventPost, user, newComment);

        commentRepository.save(newComment);

        try {
            eventApplicationRepository.saveAndFlush(newEventApplication);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(HttpStatus.CONFLICT, "application_already_exists");
        }

        eventPost.increaseApplicationCount();
        postRepository.increaseCommentCount(post.getId());

        return new CommentResponseDto(newComment);
    }

    // 댓글 조회
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getComments(Long postId) {
        postRepository.findById(postId).orElseThrow(() -> new NotFoundException("post_not_found"));
        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtDesc(postId);

        Map<Long, List<Comment>> childrenMap = comments.stream()
                .filter(comment -> comment.getParent() != null)
                .collect(Collectors.groupingBy(
                        comment -> comment.getParent().getId()
                ));

        return comments.stream()
                .filter(comment -> comment.getParent() == null)
                .map(comment -> toCommentResponseDto(comment, childrenMap))
                .toList();
    }

    // 댓글 수정
    @Transactional
    public CommentResponseDto updateComment(Long postId, Long commentId, Long userId, CommentRequestDto request) {
        postRepository.findById(postId).orElseThrow(() -> new NotFoundException("post_not_found"));
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("comment_not_found"));

        validateCommentBelongsToPost(comment, postId);
        validateCommentIsNotDeleted(comment);

        // 본인이 작성한 댓글이 아닐 때
        if (!comment.getUser().getId().equals(userId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "comment_update_forbidden");
        }

        // 이벤트 참여 댓글은 수정 불가
        if (eventApplicationRepository.existsByCommentId(commentId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "event_comment_update_forbidden");
        }

        comment.updateContent(request.getContent());

        return new CommentResponseDto(comment);

    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long postId, Long commentId, Long userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("post_not_found"));
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("comment_not_found"));

        validateCommentBelongsToPost(comment, postId);

        // 본인이 작성한 댓글이 아닐 때
        if (!comment.getUser().getId().equals(userId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "comment_delete_forbidden");
        }

        // 이미 삭제된 댓글일 때
        if (comment.getStatus() == StatusType.DELETED) {
            throw new BusinessException(HttpStatus.CONFLICT, "comment_already_deleted");
        }

        // 이벤트 참여 댓글은 삭제 불가
        if (eventApplicationRepository.existsByCommentId(commentId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "event_comment_delete_forbidden");
        }

        // 삭제 상태가 아닌 경우에 삭제 처리
        comment.updateStatusDeleted();
    }

    // 부모 댓글 검증 후 반환
    private Optional<Comment> validateAndGetParentComment(Long parentId, Long postId) {
        if (parentId == null) {
            return Optional.empty();
        }

        Comment parent = commentRepository.findById(parentId)
                .orElseThrow(() -> new NotFoundException("parent_comment_not_found"));

        validateCommentBelongsToPost(parent, postId);

        if (parent.getStatus() == StatusType.DELETED) {
            throw new NotFoundException("parent_comment_not_found");
        }

        if (parent.getParent() != null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "comment_depth_exceeded");
        }

        return Optional.of(parent);
    }

    // 게시글 ID 검증
    private void validateCommentBelongsToPost(Comment comment, Long postId) {
        if (!comment.getPost().getId().equals(postId)) {
            throw new NotFoundException("comment_not_found");
        }
    }

    // 삭제 검증
    private void validateCommentIsNotDeleted(Comment comment) {
        if (comment.getStatus() == StatusType.DELETED) {
            throw new NotFoundException("comment_not_found");
        }
    }

    // responseDto에 맞게 변환
    private CommentResponseDto toCommentResponseDto(
            Comment comment,
            Map<Long, List<Comment>> childrenMap
    ) {

        List<CommentResponseDto> childComments = childrenMap
                .getOrDefault(comment.getId(), List.of())
                .stream()
                .map(CommentResponseDto::new)
                .toList();

        return new CommentResponseDto(comment, childComments);
    }
}
