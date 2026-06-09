package org.example.week4.repository;

import lombok.Getter;
import org.example.week4.domain.Post;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class MemoryPostRepository implements PostRepository {
    @Getter
    private final ConcurrentHashMap<Long, Post> posts = new ConcurrentHashMap<>();
    private final AtomicLong counter = new AtomicLong(1);

    public void init(List<Post> initialPosts) {
        posts.clear();

        long maxId = 0;

        for (Post post : initialPosts) {
            posts.put(post.getId(), post);
            maxId = Math.max(maxId, post.getId());
        }

        counter.set(maxId + 1);
    }

    public List<Post> findAll() {
        return new ArrayList<>(posts.values());
    }

    public Optional<Post> findById(Long postId) {
        return posts.values().stream().filter(post -> post.getId().equals(postId)).findFirst();
    }

    public Post save(Post post) {
        Long id = post.getId();

        if (id != null) {
            Long savedId = id;
            counter.updateAndGet(current -> Math.max(current, savedId + 1));
            posts.put(id, post);
            return post;
        }

        id = counter.getAndIncrement();

        Post savedPost = new Post(
                id,
                post.getUserId(),
                post.getTitle(),
                post.getContent(),
                post.getFileId()
        );
        posts.put(id, savedPost);
        return savedPost;
    }

    public void deleteById(Long id) {
        posts.remove(id);
    }
}
