package org.ktb.week6.repository;

import org.ktb.week6.entity.EventApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventApplicationRepository extends JpaRepository<EventApplication, Long> {
    boolean existsByEventPostIdAndUserId(Long postId, Long userId);
    boolean existsByCommentId(Long commentId);
}
