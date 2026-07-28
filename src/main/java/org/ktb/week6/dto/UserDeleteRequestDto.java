package org.ktb.week6.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserDeleteRequestDto {

    @Size(max = 255, message = "delete_reason_too_long")
    @NotBlank(message = "delete_reason_required")
    private String reason;
}
