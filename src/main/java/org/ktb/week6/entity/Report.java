package org.ktb.week6.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.ktb.week6.enums.ReportReason;
import org.ktb.week6.enums.ReportStatusType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_report", uniqueConstraints = {
        @UniqueConstraint(
                name = "POST_REPORT_POST_USER_UNIQUE",
                columnNames = {"post_id", "user_id"}
        )
})
@Getter
@EntityListeners(AuditingEntityListener.class)
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReason reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatusType status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected Report() {
    }

    public Report(Long id, Post post, User user, ReportReason reason) {
        this.id = id;
        this.reason = reason;
        this.status = ReportStatusType.PENDING;
        this.post = post;
        this.user = user;
    }

    public void updateStatusPending() {
        this.status = ReportStatusType.PENDING;
    }

    public void updateStatusReviewed() {
        this.status = ReportStatusType.REVIEWED;
    }
}
