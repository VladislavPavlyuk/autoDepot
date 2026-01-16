package com.example.autodepot.service;

import com.example.autodepot.repository.OrderRepository;
import com.example.autodepot.service.generation.OrderCountStrategy;
import com.example.autodepot.service.generation.OrderGenerator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class OrderGenerationService {
    private final OrderRepository orderRepository;
    private final OrderGenerator orderGenerator;
    private final OrderCountStrategy orderCountStrategy;

    public OrderGenerationService(OrderRepository orderRepository,
                                  OrderGenerator orderGenerator,
                                  OrderCountStrategy orderCountStrategy) {
        this.orderRepository = orderRepository;
        this.orderGenerator = orderGenerator;
        this.orderCountStrategy = orderCountStrategy;
    }

    @Scheduled(cron = "0 0 9 * * ?")
    public void generateDailyOrders() {
        int orderCount = orderCountStrategy.getOrderCount();

        for (int i = 0; i < orderCount; i++) {
            orderRepository.save(orderGenerator.generate());
        }
    }

    public void generateRandomOrder() {
        orderRepository.save(orderGenerator.generate());
    }
}
