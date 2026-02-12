package com.example.autodepot.service;

import com.example.autodepot.dto.DriverCreateDTO;

public interface DriverApplicationService {

    void createDriver(DriverCreateDTO dto);

    void updateDriver(Long id, DriverCreateDTO dto);

    /** Parses raw JSON body to DriverCreateDTO. Throws BadRequestException on invalid input. */
    DriverCreateDTO parseDriverPayload(byte[] rawBytes);
}
