package com.tastopia.tastopia.exception;

public class DuplicateRestaurantNameException extends RuntimeException {
    public DuplicateRestaurantNameException(String message) {
        super(message);
    }
}