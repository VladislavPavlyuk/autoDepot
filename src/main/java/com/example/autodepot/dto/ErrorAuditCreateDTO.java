package com.example.autodepot.dto;

import java.time.Instant;

public record ErrorAuditCreateDTO(
    Instant createdAt,
    String threadName,
    String location,
    String exceptionType,
    String message
) {}
