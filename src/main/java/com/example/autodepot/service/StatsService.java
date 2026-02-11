package com.example.autodepot.service;

import com.example.autodepot.dto.CargoByDestinationDTO;
import com.example.autodepot.dto.DriverEarningsDTO;
import com.example.autodepot.dto.DriverPerformanceDTO;
import com.example.autodepot.dto.StatsSummaryDTO;

import java.util.List;

public interface StatsService {

    List<DriverPerformanceDTO> getDriverPerformance();

    List<CargoByDestinationDTO> getCargoByDestination();

    List<DriverEarningsDTO> getDriverEarnings();

    String getMostProfitable();

    StatsSummaryDTO getAllStats();
}
