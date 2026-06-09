package org.example.week4.handler;

import org.example.week4.enums.ApiResultStatus;
import org.example.week4.exception.BusinessException;
import org.example.week4.exception.NotFoundException;
import org.example.week4.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NotFoundException e) {
        return ResponseEntity.status(e.getStatus()).body(ApiResponse.of(ApiResultStatus.FAIL, e.getCode(), null));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
        return ResponseEntity.status(e.getStatus()).body(ApiResponse.of(ApiResultStatus.FAIL, e.getCode(), null));
    }
}
