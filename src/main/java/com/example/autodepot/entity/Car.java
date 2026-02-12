package com.example.autodepot.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Car {
    @Getter(AccessLevel.NONE)
    private Long id;
    private double capacity;
    private boolean broken;
    private Integer version;

    public Car(double capacity) {
        this(null, capacity, false, null);
    }

    public Long getId() {
        return id;
    }
}
