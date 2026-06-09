package org.example.week4.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequestDto {
    @NotBlank(message = "email_required")
    @Email(message = "invalid_email_format")
    private String email;

    @NotBlank(message = "password_required")
    @Pattern(
            regexp = "^\\s*$|^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,20}$",
            message = "invalid_password_format"
    )
    private String password;
}
