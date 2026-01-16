package com.example.autodepot.service;

import com.example.autodepot.service.generation.OrderCountStrategy;
import com.example.autodepot.service.generation.OrderGenerator;
import com.example.autodepot.service.data.OrderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class OrderGenerationService {
    private final OrderService orderService;
    private final OrderGenerator orderGenerator;
    private final OrderCountStrategy orderCountStrategy;

    public OrderGenerationService(OrderService orderService,
                                  OrderGenerator orderGenerator,
                                  OrderCountStrategy orderCountStrategy) {
        this.orderService = orderService;
        this.orderGenerator = orderGenerator;
        this.orderCountStrategy = orderCountStrategy;
    }

    @Scheduled(cron = "0 0 9 * * ?")
    public void generateDailyOrders() {
        int orderCount = orderCountStrategy.getOrderCount();

        for (int i = 0; i < orderCount; i++) {
            orderService.save(orderGenerator.generate());
        }
    }

    public void generateRandomOrder() {
        orderService.save(orderGenerator.generate());
    }
}
