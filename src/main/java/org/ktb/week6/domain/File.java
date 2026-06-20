package org.ktb.week6.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ktb.week6.enums.FileCategory;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class File {
    private Long id;
    private String path;
    private FileCategory category;
    private Long uploaderId;

    public File(Long id, String path, FileCategory category, Long uploaderId) {
        this.id = id;
        this.path = path;
        this.category = category;
        this.uploaderId = uploaderId;
    }
}
