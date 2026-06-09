package org.example.week4.loader;

import lombok.RequiredArgsConstructor;
import org.example.week4.domain.Report;
import org.example.week4.repository.ReportRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReportSeedLoader {

    private final ObjectMapper objectMapper;
    private final ReportRepository reportRepository;

    public void load() throws IOException {
        ClassPathResource resource = new ClassPathResource("mock/reports.json");

        if (!resource.exists()) {
            return;
        }

        List<Report> reports = objectMapper.readValue(
                resource.getInputStream(),
                new TypeReference<List<Report>>() {
                }
        );

        reportRepository.init(reports);
    }
}
