package org.ktb.week6.exception;

import org.springframework.http.HttpStatus;

public class AuthorizedException extends BusinessException {
    public AuthorizedException(String code) {
        super(HttpStatus.UNAUTHORIZED, code);
    }
}