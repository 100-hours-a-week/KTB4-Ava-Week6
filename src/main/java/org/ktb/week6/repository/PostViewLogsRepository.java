package org.ktb.week6.repository;

import org.ktb.week6.entity.PostViewLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostViewLogsRepository extends JpaRepository<PostViewLogs, Long> {
    Optional<PostViewLogs> findByPostIdAndUserId(Long postId, Long userId);
}
