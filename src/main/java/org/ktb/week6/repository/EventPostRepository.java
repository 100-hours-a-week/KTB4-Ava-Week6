package org.ktb.week6.repository;

import org.ktb.week6.entity.EventPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventPostRepository extends JpaRepository<EventPost, Long> {

    // 읽기 전용
    Optional<EventPost> findByPostId(Long id);

    boolean existsByPostId(Long postId);

    @Query(value = "select * from event_post where post_id = :postId for update", nativeQuery = true)
    Optional<EventPost> findByPostIdForUpdate(@Param("postId") Long postId);

}
