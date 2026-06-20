package org.ktb.week6.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.ktb.week6.enums.ApiResultStatus;

@Getter
@RequiredArgsConstructor
public class ApiResponse<T> {
    private final boolean success; // 성공 여부
    private final String message; // 반환 메시지
    private final T data; // 데이터

    public static <T> ApiResponse<T> of(ApiResultStatus status, String message, T data) {
        return new ApiResponse<T>(status.isSuccess(), message, data);
    }
}

