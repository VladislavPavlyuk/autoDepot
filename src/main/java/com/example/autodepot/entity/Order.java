package com.example.autodepot.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Getter(AccessLevel.NONE)
    private Long id;
    private String destination;
    private String cargoType;
    private double weight;
    private LocalDateTime createdAt;

    public Order(String destination, String cargoType, double weight) {
        this(null, destination, cargoType, weight, LocalDateTime.now());
    }

    public Long getId() {
        return id;
    }
}
