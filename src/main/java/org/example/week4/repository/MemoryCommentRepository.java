package org.example.week4.repository;

import org.example.week4.domain.Comment;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class MemoryCommentRepository implements CommentRepository {
    private final ConcurrentHashMap<Long, Comment> comments = new ConcurrentHashMap<>();
    private final AtomicLong counter = new AtomicLong(1);

    public void init(List<Comment> initialComments) {
        comments.clear();

        long maxId = 0;
        for (Comment comment : initialComments) {
            comments.put(comment.getId(), comment);
            maxId = Math.max(maxId, comment.getId());
        }
        counter.set(maxId + 1);
    }

    public Comment save(Comment comment) {
        Long id = comment.getId();

        if (id != null) {
            Long savedId = id;
            counter.updateAndGet(current -> Math.max(current, savedId + 1));
            comments.put(id, comment);
            return comment;
        }

        id = counter.getAndIncrement();
        Comment newComment = new Comment(id, comment.getPostId(), comment.getUserId(), comment.getParentId(), comment.getContent());
        comments.put(id, newComment);
        return newComment;
    }

    public Optional<Comment> findByCommentId(Long id) {
        Comment comment = comments.values().stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
        return Optional.ofNullable(comment);
    }

    public List<Comment> findByPostId(Long postId) {
        return comments.values().stream()
                .filter(comment -> comment.getPostId().equals(postId))
                .toList();
    }

    public void deleteById(Long id) {
        comments.remove(id);
    }

}
