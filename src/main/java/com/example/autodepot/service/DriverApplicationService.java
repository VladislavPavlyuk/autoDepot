package com.example.autodepot.service;

import com.example.autodepot.dto.DriverCreateDTO;

public interface DriverApplicationService {

    void createDriver(DriverCreateDTO dto);

    void updateDriver(Long id, DriverCreateDTO dto);

    DriverCreateDTO parseDriverPayload(byte[] rawBytes);
}
