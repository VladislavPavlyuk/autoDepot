package com.example.autodepot.service.simulation;

import com.example.autodepot.entity.Trip;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Component
public class RandomBreakdownSimulator implements BreakdownSimulator {
    private static final double BREAKDOWN_PROBABILITY = 0.1;
    private final Random random;

    public RandomBreakdownSimulator() {
        this(new Random());
    }

    RandomBreakdownSimulator(Random random) {
        this.random = random;
    }

    @Override
    public Optional<Trip> chooseTripToBreak(List<Trip> activeTrips) {
        if (activeTrips.isEmpty()) {
            return Optional.empty();
        }

        if (random.nextDouble() >= BREAKDOWN_PROBABILITY) {
            return Optional.empty();
        }

        Trip trip = activeTrips.get(random.nextInt(activeTrips.size()));
        return Optional.of(trip);
    }
}
