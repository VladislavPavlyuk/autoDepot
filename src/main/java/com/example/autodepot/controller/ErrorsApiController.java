package com.example.autodepot.controller;

import com.example.autodepot.dto.ErrorAuditDTO;
import com.example.autodepot.entity.ErrorAudit;
import com.example.autodepot.repository.ErrorAuditRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/errors")
public class ErrorsApiController {

    private final ErrorAuditRepository errorAuditRepository;

    public ErrorsApiController(ErrorAuditRepository errorAuditRepository) {
        this.errorAuditRepository = errorAuditRepository;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String exceptionType,
            @RequestParam(required = false) Long sinceEpochMilli
    ) {
        size = Math.min(Math.max(1, size), 100);
        Pageable pageable = PageRequest.of(page, size);
        Instant since = sinceEpochMilli != null ? Instant.ofEpochMilli(sinceEpochMilli) : null;
        Page<ErrorAudit> result = errorAuditRepository.findAllFiltered(exceptionType, since, pageable);
        var content = result.getContent().stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(Map.of(
                "content", content,
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages(),
                "number", result.getNumber(),
                "size", result.getSize()
        ));
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
