package com.example.autodepot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private Long id;
    private String destination;
    private String cargoType;
    private double weight;
    private LocalDateTime createdAt;

    public Order(String destination, String cargoType, double weight) {
        this(null, destination, cargoType, weight, LocalDateTime.now());
    }
}
