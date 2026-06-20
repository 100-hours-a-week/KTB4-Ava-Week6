package org.ktb.week6.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.ktb.week6.enums.FileCategory;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class File {
    @Id @GeneratedValue
    @Column(name = "file_id")
    private Long id;

    private String path;
    private String url;
    private FileCategory category;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    public File(String path, String url, FileCategory category) {
        this.path = path;
        this.url = url;
        this.category = category;
    }
}
