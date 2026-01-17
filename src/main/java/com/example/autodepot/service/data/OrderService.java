package com.example.autodepot.service.data;

import com.example.autodepot.entity.Order;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    List<Order> findAll();

    Optional<Order> findById(Long id);

    Order save(Order order);
}
