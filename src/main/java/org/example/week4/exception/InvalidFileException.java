package org.example.week4.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

@Getter
public class InvalidFileException extends BusinessException {
    private final Map<String, String> errors;

    public InvalidFileException(String fieldName, String message) {
        super(HttpStatus.UNPROCESSABLE_CONTENT, "invalid_input");

        this.errors = new HashMap<>();
        this.errors.put(fieldName, message);
    }
}
