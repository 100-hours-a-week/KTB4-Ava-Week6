package org.ktb.week6.repository;

import org.ktb.week6.entity.Post;
import org.ktb.week6.enums.StatusType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = {"user", "user.file", "file", "eventPost"})
    List<Post> findByStatusNotAndIdLessThanOrderByIdDesc(StatusType status, Long cursorId, Pageable pageable);

    Optional<Post> findByStatusNotAndId(StatusType status, Long postId);

    @Query("""
            select p from Post p left join fetch p.eventPost where p.id = :postId
            """)
    Optional<Post> findByIdWithEventPost(Long postId);

    @Modifying
    @Query("update Post p set p.commentCount = p.commentCount + 1 where p.id = :postId")
    void increaseCommentCount(@Param("postId") Long postId);

}