package com.example.autodepot.service;

import com.example.autodepot.entity.Order;
import com.example.autodepot.repository.OrderRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
public class OrderGenerationService {
    private final OrderRepository orderRepository;
    private final Random random = new Random();

    private static final List<String> DESTINATIONS = Arrays.asList(
        "New York", "Los Angeles", "Chicago", "Houston", 
        "Phoenix", "Philadelphia", "San Antonio", "San Diego"
    );

    private static final List<String> CARGO_TYPES = Arrays.asList(
        "STANDARD", "FRAGILE", "HAZARDOUS", "OVERSIZED"
    );

    public OrderGenerationService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Scheduled(cron = "0 0 9 * * ?") // Every day at 9:00
    public void generateDailyOrders() {
        int orderCount = random.nextInt(5) + 3; // From 3 to 7 orders per day

        for (int i = 0; i < orderCount; i++) {
            String destination = DESTINATIONS.get(random.nextInt(DESTINATIONS.size()));
            String cargoType = CARGO_TYPES.get(random.nextInt(CARGO_TYPES.size()));
            double weight = 500 + random.nextDouble() * 4500; // From 500 to 5000 kg

            Order order = new Order(destination, cargoType, weight);
            orderRepository.save(order);
        }
    }

    public void generateRandomOrder() {
        String destination = DESTINATIONS.get(random.nextInt(DESTINATIONS.size()));
        String cargoType = CARGO_TYPES.get(random.nextInt(CARGO_TYPES.size()));
        double weight = 500 + random.nextDouble() * 4500;

        Order order = new Order(destination, cargoType, weight);
        orderRepository.save(order);
    }
}
