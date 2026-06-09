package org.example.week4.loader;

import lombok.RequiredArgsConstructor;
import org.example.week4.domain.Like;
import org.example.week4.repository.LikeRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LikeSeedLoader {
    private final ObjectMapper objectMapper;
    private final LikeRepository likeRepository;

    public void load() throws IOException {
        ClassPathResource resource = new ClassPathResource("mock/likes.json");

        if (!resource.exists()) {
            return;
        }

        List<Like> likes = objectMapper.readValue(
                resource.getInputStream(),
                new TypeReference<List<Like>>() {
                }
        );

        likeRepository.init(likes);
    }
}
