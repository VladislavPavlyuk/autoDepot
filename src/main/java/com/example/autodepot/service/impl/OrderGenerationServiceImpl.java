package com.example.autodepot.service.impl;

import com.example.autodepot.service.OrderGenerationService;
import com.example.autodepot.service.data.OrderService;
import com.example.autodepot.service.generation.OrderCountStrategy;
import com.example.autodepot.service.generation.OrderGenerator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderGenerationServiceImpl implements OrderGenerationService {

    private final OrderService orderService;
    private final OrderGenerator orderGenerator;
    private final OrderCountStrategy orderCountStrategy;

    public OrderGenerationServiceImpl(OrderService orderService,
                                      OrderGenerator orderGenerator,
                                      OrderCountStrategy orderCountStrategy) {
        this.orderService = orderService;
        this.orderGenerator = orderGenerator;
        this.orderCountStrategy = orderCountStrategy;
    }

    @Override
    @Scheduled(cron = "0 0 9 * * ?")
    public void generateDailyOrders() {
        int orderCount = orderCountStrategy.getOrderCount();
        for (int i = 0; i < orderCount; i++) {
            orderService.save(orderGenerator.generate());
        }
    }

    @Override
    @Transactional
    public void generateRandomOrder() {
        orderService.save(orderGenerator.generate());
    }
}
