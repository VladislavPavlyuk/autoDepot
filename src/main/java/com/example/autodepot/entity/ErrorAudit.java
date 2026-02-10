package com.example.autodepot.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "error_audit")
public class ErrorAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "thread_name", length = 255)
    private String threadName;

    @Column(length = 512)
    private String location;

    @Column(name = "exception_type", nullable = false, length = 512)
    private String exceptionType;

    @Column(length = 2048)
    private String message;

    public ErrorAudit() {}

    public ErrorAudit(Instant createdAt, String threadName, String location, String exceptionType, String message) {
        this.createdAt = createdAt;
        this.threadName = threadName;
        this.location = location;
        this.exceptionType = exceptionType;
        this.message = message;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public String getThreadName() { return threadName; }
    public void setThreadName(String threadName) { this.threadName = threadName; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getExceptionType() { return exceptionType; }
    public void setExceptionType(String exceptionType) { this.exceptionType = exceptionType; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
