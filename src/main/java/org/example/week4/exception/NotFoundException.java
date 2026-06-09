package org.example.week4.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BusinessException {
    public NotFoundException(String code) {
        super(HttpStatus.NOT_FOUND, code);
    }
}

