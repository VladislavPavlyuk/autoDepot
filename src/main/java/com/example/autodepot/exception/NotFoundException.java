package com.example.autodepot.exception;

/**
 * Resource not found: order, trip, driver, etc.
 * Handled by ApiExceptionHandler → 404.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
