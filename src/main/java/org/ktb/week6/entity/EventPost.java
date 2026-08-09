package org.ktb.week6.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
public class EventPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false, unique = true)
    private Post post;

    @Column(nullable = false)
    private int capacity;

    private int applicationCount;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "eventPost")
    private List<EventApplication> eventApplication = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime deadline;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected EventPost() {
    }

    public EventPost(Post post, int capacity, LocalDateTime deadline) {
        if (capacity < 2) {
            throw new IllegalArgumentException("capacity must be at least 2");
        }
        this.post = post;
        this.capacity = capacity;
        this.applicationCount = 1;
        this.deadline = deadline;
    }

    public boolean isFull() {
        return applicationCount >= this.getCapacity();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.getDeadline());
    }

    public void increaseApplicationCount() {
        this.applicationCount++;
    }
}
