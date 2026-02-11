package com.example.autodepot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Car {
    private Long id;
    private double capacity;
    private boolean broken;
    private Integer version;

    public Car(double capacity) {
        this(null, capacity, false, null);
    }
}
