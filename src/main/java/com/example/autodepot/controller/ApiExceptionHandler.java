package com.example.autodepot.controller;

import com.example.autodepot.exception.BadRequestException;
import com.example.autodepot.exception.ExceptionCollector;
import com.example.autodepot.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/**
 * Centralized exception handling for REST API (/api/**).
 * All API exceptions are converted to JSON { "message": "..." } here and recorded in the audit log.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);
    private static final String GENERIC_ERROR = "Internal server error";

    private final ExceptionCollector exceptionCollector;

    public ApiExceptionHandler(ExceptionCollector exceptionCollector) {
        this.exceptionCollector = exceptionCollector;
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NotFoundException ex) {
        exceptionCollector.record(ex, "ApiExceptionHandler.handleNotFound");
        String message = ex.getMessage() != null ? ex.getMessage() : "Not found";
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("message", message));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(BadRequestException ex) {
        exceptionCollector.record(ex, "ApiExceptionHandler.handleBadRequest");
        String message = ex.getMessage() != null ? ex.getMessage() : "Bad request";
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("message", message));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatus(ResponseStatusException ex) {
        exceptionCollector.record(ex, "ApiExceptionHandler.handleResponseStatus");
        String message = ex.getReason() != null ? ex.getReason() : ex.getStatusCode().toString();
        return ResponseEntity
            .status(ex.getStatusCode())
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("message", message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleNotReadable(HttpMessageNotReadableException ex) {
        exceptionCollector.record(ex, "ApiExceptionHandler.handleNotReadable");
        String message = "Invalid or missing request body. Expected JSON: name, licenseYear, licenseCategories (array).";
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("message", message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAny(Exception ex) {
        exceptionCollector.record(ex, "ApiExceptionHandler.handleAny");
        log.error("API error", ex);
        Throwable root = rootCause(ex);
        String message = root.getMessage() != null && !root.getMessage().isBlank()
            ? root.getMessage()
            : GENERIC_ERROR;
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("message", message));
    }

    private static Throwable rootCause(Throwable t) {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t;
    }
}
