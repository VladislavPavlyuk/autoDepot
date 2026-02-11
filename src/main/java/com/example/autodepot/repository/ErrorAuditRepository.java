package com.example.autodepot.repository;

import com.example.autodepot.entity.ErrorAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface ErrorAuditRepository {

    ErrorAudit save(ErrorAudit audit);

    Page<ErrorAudit> findAllFiltered(String exceptionType, Instant since, Pageable pageable);

    void deleteAll();
}
