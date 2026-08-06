package org.ktb.week6.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ktb.week6.enums.PostType;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostRequestDto {

    @NotBlank(message = "title_content_type_required")
    @Size(max = 26, message = "title_too_long")
    private String title;

    @NotBlank(message = "title_content_type_required")
    private String content;

    @Positive(message = "invalid_temporary_post_id")
    private Long temporaryPostId;

    @NotNull(message = "title_content_type_required")
    private PostType type;

    private int capacity;

    private LocalDateTime deadline;

    @AssertTrue(message = "invalid_capacity")
    public boolean isCapacityValid() {
        return type != PostType.MEETING || capacity >= 1;
    }

    @AssertTrue(message = "invalid_deadline")
    public boolean isDeadlineValid() {
        return type != PostType.MEETING || (deadline != null && deadline.isAfter(LocalDateTime.now()));
    }
}
