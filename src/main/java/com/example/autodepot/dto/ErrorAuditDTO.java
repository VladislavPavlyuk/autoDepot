package com.example.autodepot.dto;

import java.time.Instant;

public record ErrorAuditDTO(
    Long id,
    Instant createdAt,
    String threadName,
    String location,
    String exceptionType,
    String message
) {}
