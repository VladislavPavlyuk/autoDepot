package com.example.autodepot.dto;

import java.util.List;

public class StatsSummaryDTO {
    private String mostProfitableDriver;
    private List<DriverPerformanceDTO> driverPerformance;
    private List<DriverEarningsDTO> driverEarnings;
    private List<CargoByDestinationDTO> cargoByDestination;

    public String getMostProfitableDriver() {
        return mostProfitableDriver;
    }

    public void setMostProfitableDriver(String mostProfitableDriver) {
        this.mostProfitableDriver = mostProfitableDriver;
    }

    public List<DriverPerformanceDTO> getDriverPerformance() {
        return driverPerformance;
    }

    public void setDriverPerformance(List<DriverPerformanceDTO> driverPerformance) {
        this.driverPerformance = driverPerformance;
    }

    public List<DriverEarningsDTO> getDriverEarnings() {
        return driverEarnings;
    }

    public void setDriverEarnings(List<DriverEarningsDTO> driverEarnings) {
        this.driverEarnings = driverEarnings;
    }

    public List<CargoByDestinationDTO> getCargoByDestination() {
        return cargoByDestination;
    }

    public void setCargoByDestination(List<CargoByDestinationDTO> cargoByDestination) {
        this.cargoByDestination = cargoByDestination;
    }
}
