package com.example.autodepot.service;

import com.example.autodepot.dto.ErrorAuditCreateDTO;
import com.example.autodepot.dto.ErrorAuditDTO;
import org.springframework.data.domain.Page;

import java.time.Instant;

public interface ErrorAuditService {

    void record(ErrorAuditCreateDTO dto);

    Page<ErrorAuditDTO> listErrors(int page, int size, String exceptionType, Instant since);
}
