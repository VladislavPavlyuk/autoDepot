package com.example.autodepot.service.generation;

import com.example.autodepot.entity.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
public class RandomOrderGenerator implements OrderGenerator {
    private static final List<String> DESTINATIONS = Arrays.asList(
        "Berlin", "Paris", "Madrid", "Rome", "Amsterdam",
        "Vienna", "Warsaw", "Brussels", "Prague", "Budapest"
    );

    private static final List<String> CARGO_TYPES = Arrays.asList(
        "STANDARD", "FRAGILE", "HAZARDOUS", "OVERSIZED"
    );

    private final Random random;

    public RandomOrderGenerator() {
        this(new Random());
    }

    RandomOrderGenerator(Random random) {
        this.random = random;
    }

    @Override
    public Order generate() {
        String destination = DESTINATIONS.get(random.nextInt(DESTINATIONS.size()));
        String cargoType = CARGO_TYPES.get(random.nextInt(CARGO_TYPES.size()));
        double weight = 500 + random.nextDouble() * 4500;
        return new Order(destination, cargoType, weight);
    }
}
