package org.example.week4.repository;

import org.example.week4.domain.Post;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository {
    void init(List<Post> posts);

    List<Post> findAll();

    Optional<Post> findById(Long postId);

    Post save(Post post);

    void deleteById(Long postId);
}

