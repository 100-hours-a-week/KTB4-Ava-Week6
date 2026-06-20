package org.ktb.week6.handler;

import org.ktb.week6.enums.ApiResultStatus;
import org.ktb.week6.exception.BusinessException;
import org.ktb.week6.exception.NotFoundException;
import org.ktb.week6.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.of(ApiResultStatus.FAIL, "지원하지 않는 HTTP 메서드입니다.", null));
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handTypeMismacth(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest().body(ApiResponse.of(ApiResultStatus.FAIL, "요청 파라미터 타입이 올바르지 않습니다.", null));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(ApiResponse.of(ApiResultStatus.FAIL, e.getMessage(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotValid(MethodArgumentNotValidException e) {
        return ResponseEntity.unprocessableContent().body(ApiResponse.of(ApiResultStatus.FAIL, e.getMessage(), null));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NotFoundException e) {
        return ResponseEntity.status(e.getStatus()).body(ApiResponse.of(ApiResultStatus.FAIL, e.getMessage(), null));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
        return ResponseEntity.status(e.getStatus()).body(ApiResponse.of(ApiResultStatus.FAIL, e.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        return ResponseEntity.internalServerError()
                .body(ApiResponse.of(ApiResultStatus.FAIL, "서버 내부 오류가 발생했습니다.", null));
    }
}
