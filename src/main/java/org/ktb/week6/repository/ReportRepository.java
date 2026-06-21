package org.ktb.week6.repository;

import org.ktb.week6.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    Optional<Report> findByPostIdAndUserId(Long postId, Long userId);
}
