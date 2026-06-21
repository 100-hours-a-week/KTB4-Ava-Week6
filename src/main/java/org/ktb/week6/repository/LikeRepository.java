package org.ktb.week6.repository;

import org.ktb.week6.entity.Like;
import org.ktb.week6.entity.Post;
import org.ktb.week6.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByPostAndUser(Post post, User user);
}
