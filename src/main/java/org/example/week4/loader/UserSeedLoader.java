package org.example.week4.loader;

import lombok.RequiredArgsConstructor;
import org.example.week4.domain.User;
import org.example.week4.repository.UserRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserSeedLoader {
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    public void load() throws IOException {
        ClassPathResource resource = new ClassPathResource("mock/users.json");
        if (!resource.exists()) {
            return;
        }

        List<User> users = objectMapper.readValue(
                resource.getInputStream(),
                new TypeReference<List<User>>() {
                }
        );

        userRepository.init(users);
    }
}
