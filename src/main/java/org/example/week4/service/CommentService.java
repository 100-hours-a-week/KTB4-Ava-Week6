package org.example.week4.service;

import lombok.RequiredArgsConstructor;
import org.example.week4.domain.Comment;
import org.example.week4.domain.Post;
import org.example.week4.domain.User;
import org.example.week4.dto.CommentRequestDto;
import org.example.week4.dto.CommentResponseDto;
import org.example.week4.exception.BusinessException;
import org.example.week4.exception.NotFoundException;
import org.example.week4.repository.CommentRepository;
import org.example.week4.repository.PostRepository;
import org.example.week4.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        if (Boolean.TRUE.equals(post.getIsDeleted())) {
            throw new NotFoundException("post_not_found");
        }
        validateParentComment(request.getParentId(), postId);
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("user_not_found"));

        Comment comment = new Comment(
                null,
                postId,
                userId,
                request.getParentId(),
                request.getContent()
        );

        Comment savedComment = commentRepository.save(comment);
        post.increaseCommentCount();
        postRepository.save(post);

        return new CommentResponseDto(savedComment, user);
    }

    // 댓글 조회
    public List<CommentResponseDto> getComments(Long postId) {
        postRepository.findById(postId).orElseThrow(() -> new NotFoundException("post_not_found"));
        List<Comment> comments = commentRepository.findByPostId(postId);

        Map<Long, List<Comment>> childrenMap = comments.stream()
                .filter(comment -> comment.getParentId() != null)
                .collect(Collectors.groupingBy(Comment::getParentId));

        Map<Long, User> userCache = new HashMap<>();

        return comments.stream()
                .filter(comment -> comment.getParentId() == null)
                .map(comment -> toCommentResponseDto(comment, childrenMap, userCache))
                .toList();
    }

    // 댓글 수정
    public CommentResponseDto updateComment(Long postId, Long commentId, Long userId, CommentRequestDto request) {
        postRepository.findById(postId).orElseThrow(() -> new NotFoundException("post_not_found"));
        Comment comment = commentRepository.findByCommentId(commentId).orElseThrow(() -> new NotFoundException("comment_not_found"));

        validateCommentBelongsToPost(comment, postId);
        validateCommentIsNotDeleted(comment);

        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "comment_update_forbidden");
        }

        User user = userRepository.findById(comment.getUserId()).orElseThrow(() -> new NotFoundException("user_not_found"));

        comment.updateContent(request.getContent());
        comment.updateIsEdited(true);

        commentRepository.save(comment);

        return new CommentResponseDto(comment, user);

    }

    // 댓글 삭제
    public void deleteComment(Long postId, Long commentId, Long userId) {
        postRepository.findById(postId).orElseThrow(() -> new NotFoundException("post_not_found"));
        Comment comment = commentRepository.findByCommentId(commentId).orElseThrow(() -> new NotFoundException("comment_not_found"));

        validateCommentBelongsToPost(comment, postId);

        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "comment_delete_forbidden");
        }

        if (!Boolean.TRUE.equals(comment.getIsDeleted())) {
            comment.updateIsDeleted(true);
            commentRepository.save(comment);

            Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("post_not_found"));
            post.decreaseCommentCount();
            postRepository.save(post);
        }
    }

    private void validateParentComment(Long parentId, Long postId) {
        if (parentId == null) {
            return;
        }

        Comment parent = commentRepository.findByCommentId(parentId)
                .orElseThrow(() -> new NotFoundException("parent_comment_not_found"));

        validateCommentBelongsToPost(parent, postId);

        if (Boolean.TRUE.equals(parent.getIsDeleted())) {
            throw new NotFoundException("parent_comment_not_found");
        }
    }

    private void validateCommentBelongsToPost(Comment comment, Long postId) {
        if (!comment.getPostId().equals(postId)) {
            throw new NotFoundException("comment_not_found");
        }
    }

    private void validateCommentIsNotDeleted(Comment comment) {
        if (Boolean.TRUE.equals(comment.getIsDeleted())) {
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
                comment.getUserId(),
                userId -> userRepository.findById(userId)
                        .orElseThrow(() -> new NotFoundException("user_not_found"))
        );

        List<CommentResponseDto> childComments = childrenMap
                .getOrDefault(comment.getId(), List.of())
                .stream()
                .map(child -> toCommentResponseDto(child, childrenMap, userCache))
                .toList();

        return new CommentResponseDto(comment, user, childComments);
    }
}
