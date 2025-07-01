package com.tastopia.tastopia.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) // 409 Conflict for duplicate entries
@Getter
public class DuplicateFavoriteException extends RuntimeException {
    private final String message;

    public DuplicateFavoriteException(String message) {
        super(message);
        this.message = message;
    }
}