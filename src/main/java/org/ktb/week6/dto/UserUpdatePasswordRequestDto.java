package org.ktb.week6.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdatePasswordRequestDto {

    @NotBlank(message = "old_password_required")
    private String oldPassword;

    @NotBlank(message = "new_password_required")
    @Pattern(
            regexp = "^\\s*$|^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,20}$",
            message = "invalid_password_format"
    )
    private String newPassword;
}
