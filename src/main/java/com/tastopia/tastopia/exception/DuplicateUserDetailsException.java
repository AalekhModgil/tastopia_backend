package com.tastopia.tastopia.exception;

import java.util.Map;

public class DuplicateUserDetailsException extends RuntimeException {
    private final Map<String, String> errors;

    public DuplicateUserDetailsException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}