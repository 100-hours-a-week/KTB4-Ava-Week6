package org.ktb.week6.repository;

import org.ktb.week6.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Comment save(Comment comment);

    Optional<Comment> findByCommentId(Long commentId);

    List<Comment> findByPostId(Long postId);

    void deleteById(Long commentId);
}
