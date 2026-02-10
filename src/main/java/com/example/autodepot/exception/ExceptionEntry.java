package com.example.autodepot.exception;

import java.time.Instant;

public record ExceptionEntry(
    Instant timestamp,
    String threadName,
    String location,
    String exceptionType,
    String message
) {}
