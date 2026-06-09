package org.example.week4.repository;

import org.example.week4.domain.Report;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class MemoryReportRepository implements ReportRepository {
    private final ConcurrentHashMap<Long, Report> reports = new ConcurrentHashMap<>();
    private final AtomicLong counter = new AtomicLong(1);

    public void init(List<Report> initialReports) {
        reports.clear();

        long maxId = 0;
        for (Report report : initialReports) {
            reports.put(report.getId(), report);
            maxId = Math.max(maxId, report.getId());
        }
        counter.set(maxId + 1);
    }

    @Override
    public Report save(Report report) {
        Long id = report.getId();

        if (id == null) {
            id = counter.getAndIncrement();
        } else {
            Long savedId = id;
            counter.updateAndGet(current -> Math.max(current, savedId + 1));
        }

        Report saved = new Report(id, report.getPostId(), report.getUserId(), report.getCreatedAt());
        reports.put(id, saved);
        return saved;
    }

    @Override
    public Optional<Report> findByPostIdAndUserId(Long postId, Long userId) {
        return reports.values().stream()
                .filter(report -> report.getPostId().equals(postId)
                        && report.getUserId().equals(userId))
                .findFirst();
    }
}
