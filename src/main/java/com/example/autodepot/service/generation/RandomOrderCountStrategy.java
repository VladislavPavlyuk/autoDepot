package com.example.autodepot.service.generation;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class RandomOrderCountStrategy implements OrderCountStrategy {
    private static final int MIN_COUNT = 3;
    private static final int MAX_COUNT = 7;
    private final Random random;

    public RandomOrderCountStrategy() {
        this(new Random());
    }

    RandomOrderCountStrategy(Random random) {
        this.random = random;
    }

    @Override
    public int getOrderCount() {
        return random.nextInt(MAX_COUNT - MIN_COUNT + 1) + MIN_COUNT;
    }
}
