package org.example.week4.repository;

import org.example.week4.domain.Like;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository {
    void init(List<Like> likes);

    Like save(Like like);

    Optional<Like> findByPostIdAndUserId(Long postId, Long userId);

    void deleteById(Like like);


}
