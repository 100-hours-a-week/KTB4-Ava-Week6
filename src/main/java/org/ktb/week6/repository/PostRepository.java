package org.ktb.week6.repository;

import org.ktb.week6.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAll();

    Optional<Post> findById(Long postId);

    Post save(Post post);

    void deleteById(Long postId);
}

