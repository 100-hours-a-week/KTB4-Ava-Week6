package org.ktb.week6.repository;

import org.ktb.week6.entity.PostHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostHistoryRepository extends JpaRepository<PostHistory, Long> {

    @Query("""
        SELECT COALESCE(MAX(ph.version), 0)
        FROM PostHistory ph
        WHERE ph.post.id = :postId
    """)
    Long findMaxVersionByPostId(@Param("postId") Long postId);
}
