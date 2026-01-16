package com.example.autodepot.dto;

public class CargoByDestinationDTO {
    private String destination;
    private long cargoCount;

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public long getCargoCount() {
        return cargoCount;
    }

    public void setCargoCount(long cargoCount) {
        this.cargoCount = cargoCount;
    }
}
