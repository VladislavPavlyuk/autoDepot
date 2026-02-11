package com.example.autodepot.repository;

import com.example.autodepot.AbstractPostgresTest;
import com.example.autodepot.entity.ErrorAudit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ErrorAuditRepositoryTest extends AbstractPostgresTest {

    @Autowired
    private ErrorAuditRepository errorAuditRepository;

    @BeforeEach
    void setUp() {
        errorAuditRepository.deleteAll();
    }

    @Test
    void save_WhenErrorAuditSaved_ReturnsEntityWithIdAndFields() {
        ErrorAudit audit = new ErrorAudit(
            Instant.now(), "main", "SomeService.method",
            "java.lang.RuntimeException", "Test error"
        );
        ErrorAudit saved = errorAuditRepository.save(audit);

        boolean actualResult = saved.getId() != null
            && "java.lang.RuntimeException".equals(saved.getExceptionType())
            && "Test error".equals(saved.getMessage());
        boolean expectedResult = true;
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void findAllFiltered_WhenNoFilters_ReturnsAllSaved() {
        errorAuditRepository.save(new ErrorAudit(
            Instant.now(), "t1", "loc1", "Ex1", "msg1"));
        errorAuditRepository.save(new ErrorAudit(
            Instant.now(), "t2", "loc2", "Ex2", "msg2"));

        var page = errorAuditRepository.findAllFiltered(null, null, PageRequest.of(0, 10));
        long actualResult = page.getTotalElements();
        long expectedResult = 2L;
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void findAllFiltered_WhenFilterByExceptionType_ReturnsMatchingOnly() {
        var now = Instant.now();
        errorAuditRepository.save(new ErrorAudit(now, "t1", "loc1", "Ex1", "msg1"));
        errorAuditRepository.save(new ErrorAudit(now, "t2", "loc2", "Ex1", "msg2"));
        errorAuditRepository.save(new ErrorAudit(now, "t3", "loc3", "Ex2", "msg3"));

        var page = errorAuditRepository.findAllFiltered("Ex1", null, PageRequest.of(0, 10));
        long actualResult = page.getTotalElements();
        long expectedResult = 2L;
        assertEquals(expectedResult, actualResult);
    }
}
