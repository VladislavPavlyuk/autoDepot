package com.example.autodepot.service.impl;

import com.example.autodepot.dto.ErrorAuditCreateDTO;
import com.example.autodepot.dto.ErrorAuditDTO;
import com.example.autodepot.entity.ErrorAudit;
import com.example.autodepot.mapper.ErrorAuditMapper;
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
    private final ErrorAuditMapper errorAuditMapper;

    public ErrorAuditServiceImpl(ErrorAuditRepository errorAuditRepository, ErrorAuditMapper errorAuditMapper) {
        this.errorAuditRepository = errorAuditRepository;
        this.errorAuditMapper = errorAuditMapper;
    }

    @Override
    public void record(ErrorAuditCreateDTO dto) {
        ErrorAudit audit = errorAuditMapper.toEntity(dto);
        errorAuditRepository.save(audit);
    }

    @Override
    public Page<ErrorAuditDTO> listErrors(int page, int size, String exceptionType, Instant since) {
        int clampedSize = Math.min(Math.max(MIN_SIZE, size), MAX_SIZE);
        Pageable pageable = PageRequest.of(page, clampedSize);
        Page<ErrorAudit> result = errorAuditRepository.findAllFiltered(exceptionType, since, pageable);
        return result.map(errorAuditMapper::toDto);
    }
}
