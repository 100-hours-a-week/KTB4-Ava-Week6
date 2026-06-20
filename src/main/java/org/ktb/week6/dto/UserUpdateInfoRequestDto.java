package org.ktb.week6.dto;

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
public class UserUpdateInfoRequestDto {
    @Pattern(regexp = "^\\s*$|^\\S+$", message = "nickname_no_spaces")
    @Size(max = 10, message = "nickname_too_long")
    private String nickname;
}
