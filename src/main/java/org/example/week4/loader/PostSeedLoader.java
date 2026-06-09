package org.example.week4.loader;

import lombok.RequiredArgsConstructor;
import org.example.week4.domain.Post;
import org.example.week4.repository.PostRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PostSeedLoader {
    private final ObjectMapper objectMapper;
    private final PostRepository postRepository;

    public void load() throws IOException {
        ClassPathResource resource = new ClassPathResource("mock/posts.json");

        if (!resource.exists()) {
            return;
        }

        List<Post> posts = objectMapper.readValue(
                resource.getInputStream(),
                new TypeReference<List<Post>>() {
                }
        );

        postRepository.init(posts);
    }
}
