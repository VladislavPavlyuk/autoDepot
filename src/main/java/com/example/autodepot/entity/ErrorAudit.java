package com.example.autodepot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorAudit {
    private Long id;
    private Instant createdAt;
    private String threadName;
    private String location;
    private String exceptionType;
    private String message;

    public ErrorAudit(Instant createdAt, String threadName, String location, String exceptionType, String message) {
        this(null, createdAt, threadName, location, exceptionType, message);
    }
}
