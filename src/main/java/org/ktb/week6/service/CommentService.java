package org.ktb.week6.service;

import lombok.RequiredArgsConstructor;
import org.ktb.week6.dto.CommentRequestDto;
import org.ktb.week6.dto.CommentResponseDto;
import org.ktb.week6.entity.Comment;
import org.ktb.week6.entity.Post;
import org.ktb.week6.entity.User;
import org.ktb.week6.enums.StatusType;
import org.ktb.week6.exception.BusinessException;
import org.ktb.week6.exception.NotFoundException;
import org.ktb.week6.repository.CommentRepository;
import org.ktb.week6.repository.PostRepository;
import org.ktb.week6.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    // 댓글 작성
    public CommentResponseDto createComment(CommentRequestDto request, Long postId, Long userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("post_not_found"));
        if (post.getStatus().equals(StatusType.DELETED)) {
            throw new NotFoundException("post_not_found");
        }

        validateParentComment(request.getParentId(), postId);
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("user_not_found"));


        Optional<Comment> parent = commentRepository.findByCommentId(request.getParentId());

        Comment comment = new Comment(
                request.getContent(),
                post,
                user,
                parent.get()
        );

        Comment savedComment = commentRepository.save(comment);
        postRepository.save(post);

        return new CommentResponseDto(savedComment);
    }

    // 댓글 조회
    public List<CommentResponseDto> getComments(Long postId) {
        postRepository.findById(postId).orElseThrow(() -> new NotFoundException("post_not_found"));
        List<Comment> comments = commentRepository.findByPostId(postId);

        Map<Long, List<Comment>> childrenMap = comments.stream()
                .filter(comment -> comment.getParent() != null)
                .collect(Collectors.groupingBy(
                        comment -> comment.getParent().getId()
                ));

        Map<Long, User> userCache = new HashMap<>();

        return comments.stream()
                .filter(comment -> comment.getParent() == null)
                .map(comment -> toCommentResponseDto(comment, childrenMap, userCache))
                .toList();
    }

    // 댓글 수정
    public CommentResponseDto updateComment(Long postId, Long commentId, Long userId, CommentRequestDto request) {
        postRepository.findById(postId).orElseThrow(() -> new NotFoundException("post_not_found"));
        Comment comment = commentRepository.findByCommentId(commentId).orElseThrow(() -> new NotFoundException("comment_not_found"));

        validateCommentBelongsToPost(comment, postId);
        validateCommentIsNotDeleted(comment);

        // 본인이 작성한 댓글이 아닐 때
        if (!comment.getAuth().getId().equals(userId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "comment_update_forbidden");
        }

        comment.updateContent(request.getContent());
        comment.updateIsEdited();

        commentRepository.save(comment);

        return new CommentResponseDto(comment);

    }

    // 댓글 삭제
    public void deleteComment(Long postId, Long commentId, Long userId) {
        postRepository.findById(postId).orElseThrow(() -> new NotFoundException("post_not_found"));
        Comment comment = commentRepository.findByCommentId(commentId).orElseThrow(() -> new NotFoundException("comment_not_found"));

        validateCommentBelongsToPost(comment, postId);

        // 본인이 작성한 댓글이 아닐 때
        if (!comment.getAuth().getId().equals(userId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "comment_delete_forbidden");
        }

        // 삭제 상태가 아닌 경우에 삭제 처리
        if (comment.getStatus() != StatusType.DELETED) {
            comment.updateStatus(StatusType.DELETED);
            commentRepository.save(comment);

            Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("post_not_found"));
            postRepository.save(post);
        }

        // 이미 삭제된 댓글일 때
        if (comment.getStatus() == StatusType.DELETED) {
            throw new BusinessException(HttpStatus.CONFLICT, "comment_alreay_deleted");
        }
    }

    // 부모 댓글 ID 검증
    private void validateParentComment(Long parentId, Long postId) {
        if (parentId == null) {
            return;
        }

        Comment parent = commentRepository.findByCommentId(parentId)
                .orElseThrow(() -> new NotFoundException("parent_comment_not_found"));

        validateCommentBelongsToPost(parent, postId);

        if (parent.getStatus() == StatusType.DELETED) {
            throw new NotFoundException("parent_comment_not_found");
        }
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
            Map<Long, List<Comment>> childrenMap,
            Map<Long, User> userCache
    ) {
        User user = userCache.computeIfAbsent(
                comment.getAuth().getId(),
                userId -> userRepository.findById(userId)
                        .orElseThrow(() -> new NotFoundException("user_not_found"))
        );

        List<CommentResponseDto> childComments = childrenMap
                .getOrDefault(comment.getId(), List.of())
                .stream()
                .map(child -> toCommentResponseDto(child, childrenMap, userCache))
                .toList();

        return new CommentResponseDto(comment, childComments);
    }
}
