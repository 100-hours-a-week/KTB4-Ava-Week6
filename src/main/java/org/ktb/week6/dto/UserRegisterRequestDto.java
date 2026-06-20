package org.ktb.week6.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterRequestDto {
    @NotBlank(message = "email_required")
    @Email(message = "invalid_email_format")
    private String email;

    @NotBlank(message = "password_required")
    @Pattern(
            regexp = "^\\s*$|^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,20}$",
            message = "invalid_password_format"
    )
    private String password;

    @NotBlank(message = "nickname_required")
    @Pattern(regexp = "^\\s*$|^\\S+$", message = "nickname_no_spaces")
    @Size(max = 10, message = "nickname_too_long")
    private String nickname;
}
