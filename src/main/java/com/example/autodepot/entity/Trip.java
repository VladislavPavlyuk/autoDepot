package com.example.autodepot.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class Trip {
    public enum TripStatus {
        IN_PROGRESS, COMPLETED, BROKEN, REPAIR_REQUESTED
    }

    private Long id;
    private Order order;
    private Driver driver;
    private Car car;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private TripStatus status = TripStatus.IN_PROGRESS;
    private Double payment;
    private String carStatusAfterTrip;

    public Trip(Order order, Driver driver, Car car) {
        this.order = order;
        this.driver = driver;
        this.car = car;
        this.startTime = LocalDateTime.now();
    }
}
