package org.ktb.week6.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostRequestDto {

    @NotBlank(message = "title_content_required")
    @Size(max = 26, message = "title_too_long")
    private String title;

    @NotBlank(message = "title_content_required")
    private String content;

}
