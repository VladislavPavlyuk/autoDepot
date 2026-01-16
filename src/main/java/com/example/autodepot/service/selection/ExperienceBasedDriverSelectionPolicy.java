package com.example.autodepot.service.selection;

import com.example.autodepot.entity.Driver;
import com.example.autodepot.entity.Order;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class ExperienceBasedDriverSelectionPolicy implements DriverSelectionPolicy {
    private static final Map<String, Integer> CARGO_TYPE_REQUIREMENTS = Map.of(
        "FRAGILE", 5,
        "HAZARDOUS", 10,
        "OVERSIZED", 7,
        "STANDARD", 1
    );

    @Override
    public Optional<Driver> selectDriver(Order order, List<Driver> availableDrivers) {
        int requiredExperience = CARGO_TYPE_REQUIREMENTS.getOrDefault(
            order.getCargoType().toUpperCase(), 1);

        return availableDrivers.stream()
            .filter(d -> d.getExperience() >= requiredExperience)
            .sorted(Comparator.comparing(Driver::getExperience).reversed())
            .findFirst();
    }
}
