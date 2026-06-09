package org.example.week4.repository;

import org.example.week4.domain.Comment;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository {
    void init(List<Comment> comments);

    Comment save(Comment comment);

    Optional<Comment> findByCommentId(Long commentId);

    List<Comment> findByPostId(Long postId);

    void deleteById(Long commentId);
}
