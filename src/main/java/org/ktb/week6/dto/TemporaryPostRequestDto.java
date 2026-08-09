package org.ktb.week6.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ktb.week6.enums.PostType;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TemporaryPostRequestDto {
    @Size(max = 26, message = "title_too_long")
    private String title;
    private String content;

    private PostType type;

    private int capacity;

    private LocalDateTime deadline;

}
