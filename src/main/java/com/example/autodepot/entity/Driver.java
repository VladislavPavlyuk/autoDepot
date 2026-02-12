package com.example.autodepot.entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Driver {
    @Getter(AccessLevel.NONE)
    private Long id;
    private String name;
    private Integer licenseYear;
    private List<String> licenseCategories = new ArrayList<>();
    private boolean available = true;
    private double earnings = 0.0;

    public Driver(String name, Integer licenseYear) {
        this.name = name;
        this.licenseYear = licenseYear;
        this.licenseCategories.add("B");
    }

    public int getExperience() {
        if (licenseYear == null) return 0;
        return Math.max(0, java.time.Year.now().getValue() - licenseYear);
    }

    public void addEarnings(double amount) {
        this.earnings += amount;
    }

    public Long getId() {
        return id;
    }
}
