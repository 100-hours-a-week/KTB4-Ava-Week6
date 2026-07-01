package org.ktb.week6.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
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
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @NotNull
    private ReportReason reason;

    @Enumerated(EnumType.STRING)
    @NotNull
    private ReportStatusType status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id")
    @NotNull
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    @NotNull
    private User user;

    @CreatedDate
    @NotNull
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
}
