package org.ktb.week6.repository;

import org.ktb.week6.entity.TemporaryPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TemporaryPostRepository extends JpaRepository<TemporaryPost, Long> {

    Optional<TemporaryPost> findByUserId(Long userId);

    Optional<TemporaryPost> findByIdAndUserId(Long id, Long userId);
}
