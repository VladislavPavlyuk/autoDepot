package com.example.autodepot.service.impl;

import com.example.autodepot.dto.OrderDTO;
import com.example.autodepot.entity.Order;
import com.example.autodepot.exception.BadRequestException;
import com.example.autodepot.mapper.OrderMapper;
import com.example.autodepot.service.OrderApplicationService;
import com.example.autodepot.service.data.OrderService;
import org.springframework.stereotype.Service;

@Service
public class OrderApplicationServiceImpl implements OrderApplicationService {

    private static final int MAX_STRING_LENGTH = 255;
    private static final double MAX_WEIGHT_KG = 100_000.0;

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    public OrderApplicationServiceImpl(OrderService orderService, OrderMapper orderMapper) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
    }

    @Override
    public void createOrder(OrderDTO orderDTO) {
        if (orderDTO == null) {
            throw new BadRequestException("Order payload is required");
        }
        String dest = orderDTO.getDestination() == null ? "" : orderDTO.getDestination().trim();
        String cargo = orderDTO.getCargoType() == null ? "" : orderDTO.getCargoType().trim();
        if (dest.isEmpty()) {
            throw new BadRequestException("Destination is required");
        }
        if (cargo.isEmpty()) {
            throw new BadRequestException("Cargo type is required");
        }
        if (dest.length() > MAX_STRING_LENGTH || cargo.length() > MAX_STRING_LENGTH) {
            throw new BadRequestException("Destination and cargo type must be at most " + MAX_STRING_LENGTH + " characters");
        }
        double weight = orderDTO.getWeight();
        if (weight <= 0 || weight > MAX_WEIGHT_KG) {
            throw new BadRequestException("Weight must be between 0 and " + (long) MAX_WEIGHT_KG + " kg");
        }
        Order order = orderMapper.toEntity(orderDTO);
        orderService.save(order);
    }
}
