package com.example.autodepot.exception;

import com.example.autodepot.dto.ErrorAuditCreateDTO;
import com.example.autodepot.service.ErrorAuditService;
import org.springframework.stereotype.Component;

import java.time.Instant;

/** Saves errors to the database. */
@Component
public class ExceptionCollector {

    private static final int MAX_MESSAGE_LEN = 2048;
    private static final int MAX_LOCATION_LEN = 512;
    private static final int MAX_EXCEPTION_TYPE_LEN = 512;

    private final ErrorAuditService errorAuditService;

    public ExceptionCollector(ErrorAuditService errorAuditService) {
        this.errorAuditService = errorAuditService;
    }

    public void record(Throwable ex, String location) {
        Instant now = Instant.now();
        String threadName = Thread.currentThread().getName();
        String exceptionType = ex.getClass().getName();
        String message = ex.getMessage();
        if (message != null && message.length() > MAX_MESSAGE_LEN) {
            message = message.substring(0, MAX_MESSAGE_LEN);
        }
        if (location != null && location.length() > MAX_LOCATION_LEN) {
            location = location.substring(0, MAX_LOCATION_LEN);
        }
        if (exceptionType.length() > MAX_EXCEPTION_TYPE_LEN) {
            exceptionType = exceptionType.substring(0, MAX_EXCEPTION_TYPE_LEN);
        }
        ErrorAuditCreateDTO dto = new ErrorAuditCreateDTO(now, threadName, location, exceptionType, message);
        errorAuditService.record(dto);
    }
}
