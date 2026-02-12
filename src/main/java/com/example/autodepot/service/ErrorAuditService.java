package com.example.autodepot.service;

import com.example.autodepot.dto.ErrorAuditDTO;
import org.springframework.data.domain.Page;

import java.time.Instant;

public interface ErrorAuditService {

    Page<ErrorAuditDTO> listErrors(int page, int size, String exceptionType, Instant since);
}
