package org.ktb.week6.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BusinessException {
    public UnauthorizedException(String code) {
        super(HttpStatus.UNAUTHORIZED, code);
    }
}
