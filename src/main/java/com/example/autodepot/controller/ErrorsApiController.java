package com.example.autodepot.controller;

import com.example.autodepot.dto.ErrorAuditDTO;
import com.example.autodepot.service.ErrorAuditService;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/errors")
public class ErrorsApiController {

    private final ErrorAuditService errorAuditService;

    public ErrorsApiController(ErrorAuditService errorAuditService) {
        this.errorAuditService = errorAuditService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String exceptionType,
            @RequestParam(required = false) Long sinceEpochMilli
    ) {
        Instant since = sinceEpochMilli != null ? Instant.ofEpochMilli(sinceEpochMilli) : null;
        Page<ErrorAuditDTO> result = errorAuditService.listErrors(page, size, exceptionType, since);
        var content = result.getContent();
        return ResponseEntity.ok(Map.of(
                "content", content,
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages(),
                "number", result.getNumber(),
                "size", result.getSize()
        ));
    }
}
