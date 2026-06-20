package org.ktb.week6.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResultDto {
    private TokenInfoDto token; // response body
    private String newRefreshToken; // RTR 발생 시 사용 (없으면 null)
}
