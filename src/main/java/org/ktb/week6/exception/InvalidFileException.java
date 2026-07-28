package org.ktb.week6.exception;

import org.springframework.http.HttpStatus;

public class InvalidFileException extends BusinessException {
    public InvalidFileException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
