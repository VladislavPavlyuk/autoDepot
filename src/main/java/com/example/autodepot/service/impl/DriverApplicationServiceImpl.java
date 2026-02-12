package com.example.autodepot.service.impl;

import com.example.autodepot.dto.DriverCreateDTO;
import com.example.autodepot.entity.Driver;
import com.example.autodepot.exception.BadRequestException;
import com.example.autodepot.service.DriverApplicationService;
import com.example.autodepot.service.data.DriverService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class DriverApplicationServiceImpl implements DriverApplicationService {

    private static final int MAX_NAME_LENGTH = 255;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final DriverService driverService;

    public DriverApplicationServiceImpl(DriverService driverService) {
        this.driverService = driverService;
    }

    @Override
    public DriverCreateDTO parseDriverPayload(byte[] rawBytes) {
        String rawBody = rawBytes == null || rawBytes.length == 0
            ? null
            : new String(rawBytes, StandardCharsets.UTF_8);
        if (rawBody == null || rawBody.isBlank()) {
            throw new BadRequestException("Request body is required. Send JSON: name, licenseYear, licenseCategories (array).");
        }
        try {
            DriverCreateDTO dto = OBJECT_MAPPER.readValue(rawBody, DriverCreateDTO.class);
            if (dto == null) {
                throw new BadRequestException("Driver payload is required");
            }
            return dto;
        } catch (IOException e) {
            throw new BadRequestException("Invalid JSON: " + (e.getMessage() != null ? e.getMessage() : "parse error"));
        }
    }

    @Override
    public void createDriver(DriverCreateDTO dto) {
        if (dto == null) {
            throw new BadRequestException("Driver payload is required");
        }
        String name = validateDriverName(dto.getName());
        List<String> categories = validateAndNormalizeLicenseCategories(dto.getLicenseCategories());
        int licenseYear = validateLicenseYear(dto.getLicenseYear());
        Driver driver = toDriverEntity(name, licenseYear, categories);
        driverService.save(driver);
    }

    private String validateDriverName(String name) {
        String trimmed = name == null ? "" : name.trim();
        if (trimmed.isEmpty()) {
            throw new BadRequestException("Driver name is required");
        }
        if (trimmed.length() > MAX_NAME_LENGTH) {
            throw new BadRequestException("Driver name must be at most " + MAX_NAME_LENGTH + " characters");
        }
        return trimmed;
    }

    private List<String> validateAndNormalizeLicenseCategories(List<String> categories) {
        if (categories == null || categories.isEmpty()) {
            throw new BadRequestException("At least one license category (A–E) is required");
        }
        List<String> valid = new ArrayList<>();
        for (String c : categories) {
            String cat = c == null ? "" : c.trim().toUpperCase();
            if (cat.matches("^[A-E]$") && !valid.contains(cat)) {
                valid.add(cat);
            }
        }
        if (valid.isEmpty()) {
            throw new BadRequestException("Driver license categories must be one or more of A, B, C, D, E");
        }
        return valid;
    }

    private int validateLicenseYear(Integer licenseYear) {
        int currentYear = java.time.Year.now().getValue();
        if (licenseYear == null || licenseYear < 1970 || licenseYear > currentYear) {
            throw new BadRequestException("Driver license year must be between 1970 and current year");
        }
        return licenseYear;
    }

    private Driver toDriverEntity(String name, int licenseYear, List<String> licenseCategories) {
        Driver driver = new Driver(name, licenseYear);
        driver.setLicenseCategories(licenseCategories);
        driver.setAvailable(true);
        driver.setEarnings(0.0);
        return driver;
    }
}
