package org.ktb.week6.repository;

import org.ktb.week6.entity.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @EntityGraph(attributePaths = {"user", "post", "parent"})
    List<Comment> findByPostIdOrderByCreatedAtDesc(Long postId);
}
