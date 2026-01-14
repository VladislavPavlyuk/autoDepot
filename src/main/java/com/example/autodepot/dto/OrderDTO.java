package com.example.autodepot.dto;

public class OrderDTO {
    private String destination;
    private String cargoType;
    private double weight;

    public OrderDTO() {
    }

    public OrderDTO(String destination, String cargoType, double weight) {
        this.destination = destination;
        this.cargoType = cargoType;
        this.weight = weight;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getCargoType() {
        return cargoType;
    }

    public void setCargoType(String cargoType) {
        this.cargoType = cargoType;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}
