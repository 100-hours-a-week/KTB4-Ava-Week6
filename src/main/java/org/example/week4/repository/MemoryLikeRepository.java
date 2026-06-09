package org.example.week4.repository;

import org.example.week4.domain.Like;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class MemoryLikeRepository implements LikeRepository {
    private final ConcurrentHashMap<Long, Like> likes = new ConcurrentHashMap<>();
    private final AtomicLong counter = new AtomicLong(1);

    public void init(List<Like> initialLikes) {
        likes.clear();

        long maxId = 0;
        for (Like like : initialLikes) {
            likes.put(like.getId(), like);
            maxId = Math.max(maxId, like.getId());
        }

        counter.set(maxId + 1);

    }

    public Like save(Like like) {
        Long id = like.getId();

        if (id == null) {
            id = counter.getAndIncrement();
        } else {
            Long savedId = id;
            counter.updateAndGet(current -> Math.max(current, savedId + 1));
        }

        Like newLike = new Like(
                id,
                like.getPostId(),
                like.getUserId(),
                like.getCreatedAt()
        );
        likes.put(id, newLike);
        return newLike;
    }

    public Optional<Like> findByPostIdAndUserId(Long postId, Long userId) {
        return likes.values().stream()
                .filter(like ->
                        like.getPostId().equals(postId)
                                && like.getUserId().equals(userId)
                )
                .findFirst();
    }

    public void deleteById(Like like) {
        likes.remove(like.getId());
    }
}
