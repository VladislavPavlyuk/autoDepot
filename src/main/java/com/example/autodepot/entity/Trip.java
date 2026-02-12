package com.example.autodepot.entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class Trip {
    public enum TripStatus {
        IN_PROGRESS, COMPLETED, BROKEN, REPAIR_REQUESTED
    }

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Long id;
    @Getter(AccessLevel.NONE)
    private Order order;
    @Getter(AccessLevel.NONE)
    private Driver driver;
    @Getter(AccessLevel.NONE)
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

    public Order getOrder() {
        return order;
    }

    public Driver getDriver() {
        return driver;
    }

    public Car getCar() {
        return car;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
