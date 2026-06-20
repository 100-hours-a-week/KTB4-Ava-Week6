package org.ktb.week6.repository;

import org.ktb.week6.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    Like save(Like like);

    Optional<Like> findByPostIdAndUserId(Long postId, Long userId);

    void deleteById(Like like);


}
