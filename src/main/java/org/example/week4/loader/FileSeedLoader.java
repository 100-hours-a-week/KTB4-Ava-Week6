package org.example.week4.loader;

import lombok.RequiredArgsConstructor;
import org.example.week4.domain.File;
import org.example.week4.repository.FileRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FileSeedLoader {
    private final ObjectMapper objectMapper;
    private final FileRepository fileRepository;

    public void load() throws IOException {
        ClassPathResource resource = new ClassPathResource("mock/files.json");
        if (!resource.exists()) {
            return;
        }

        List<File> files = objectMapper.readValue(
                resource.getInputStream(),
                new TypeReference<List<File>>() {
                }
        );

        fileRepository.init(files);
    }
}
