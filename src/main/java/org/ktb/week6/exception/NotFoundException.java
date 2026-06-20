package org.ktb.week6.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BusinessException {
    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}

