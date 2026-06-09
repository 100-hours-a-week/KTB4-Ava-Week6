package org.example.week4.exception;

import org.springframework.http.HttpStatus;

public class AuthorizedException extends BusinessException {
    public AuthorizedException(String code) {
        super(HttpStatus.UNAUTHORIZED, code);
    }
}