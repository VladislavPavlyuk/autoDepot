package com.example.autodepot.exception;

/**
 * Business validation error: client sent invalid data.
 * Handled centrally by {@link com.example.autodepot.controller.ApiExceptionHandler} → 400.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
