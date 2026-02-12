package com.example.autodepot.service.impl;

import com.example.autodepot.dto.ErrorAuditDTO;
import com.example.autodepot.entity.ErrorAudit;
import com.example.autodepot.repository.ErrorAuditRepository;
import com.example.autodepot.service.ErrorAuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ErrorAuditServiceImpl implements ErrorAuditService {

    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 100;

    private final ErrorAuditRepository errorAuditRepository;

    public ErrorAuditServiceImpl(ErrorAuditRepository errorAuditRepository) {
        this.errorAuditRepository = errorAuditRepository;
    }

    @Override
    public Page<ErrorAuditDTO> listErrors(int page, int size, String exceptionType, Instant since) {
        int clampedSize = Math.min(Math.max(MIN_SIZE, size), MAX_SIZE);
        Pageable pageable = PageRequest.of(page, clampedSize);
        Page<ErrorAudit> result = errorAuditRepository.findAllFiltered(exceptionType, since, pageable);
        return result.map(this::toDto);
    }

    private ErrorAuditDTO toDto(ErrorAudit e) {
        return new ErrorAuditDTO(
            e.getId(),
            e.getCreatedAt(),
            e.getThreadName(),
            e.getLocation(),
            e.getExceptionType(),
            e.getMessage()
        );
    }
}
