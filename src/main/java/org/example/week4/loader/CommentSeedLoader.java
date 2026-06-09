package org.example.week4.loader;

import lombok.RequiredArgsConstructor;
import org.example.week4.domain.Comment;
import org.example.week4.repository.CommentRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CommentSeedLoader {
    private final ObjectMapper objectMapper;
    private final CommentRepository commentRepository;

    public void load() throws IOException {
        ClassPathResource resource = new ClassPathResource("mock/comments.json");
        if (!resource.exists()) {
            return;
        }

        List<Comment> comments = objectMapper.readValue(resource.getInputStream(), objectMapper.getTypeFactory().constructCollectionType(List.class, Comment.class));

        commentRepository.init(comments);
    }
}
