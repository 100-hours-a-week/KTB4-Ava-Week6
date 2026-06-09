package org.example.week4.repository;

import org.example.week4.domain.Report;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository {

    void init(List<Report> reports);

    Report save(Report report);

    Optional<Report> findByPostIdAndUserId(Long postId, Long userId);
}
