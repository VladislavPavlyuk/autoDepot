package com.example.autodepot.service;

import com.example.autodepot.repository.OrderRepository;
import com.example.autodepot.service.generation.OrderGenerator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class OrderGenerationService {
    private final OrderRepository orderRepository;
    private final OrderGenerator orderGenerator;
    private final Random random = new Random();

    public OrderGenerationService(OrderRepository orderRepository, OrderGenerator orderGenerator) {
        this.orderRepository = orderRepository;
        this.orderGenerator = orderGenerator;
    }

    @Scheduled(cron = "0 0 9 * * ?")
    public void generateDailyOrders() {
        int orderCount = random.nextInt(5) + 3;

        for (int i = 0; i < orderCount; i++) {
            orderRepository.save(orderGenerator.generate());
        }
    }

    public void generateRandomOrder() {
        orderRepository.save(orderGenerator.generate());
    }
}
