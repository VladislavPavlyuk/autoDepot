package com.example.autodepot.service;

import com.example.autodepot.dto.OrderDTO;
import com.example.autodepot.entity.Order;
import com.example.autodepot.mapper.OrderMapper;
import com.example.autodepot.service.data.OrderService;
import org.springframework.stereotype.Service;

@Service
public class OrderApplicationService {
    private final OrderService orderService;
    private final OrderMapper orderMapper;

    public OrderApplicationService(OrderService orderService, OrderMapper orderMapper) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
    }

    public void createOrder(OrderDTO orderDTO) {
        Order order = orderMapper.toEntity(orderDTO);
        orderService.save(order);
    }
}
