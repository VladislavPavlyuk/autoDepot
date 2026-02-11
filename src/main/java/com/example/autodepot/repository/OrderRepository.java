package com.example.autodepot.repository;

import com.example.autodepot.entity.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    List<Order> findAll();

    Optional<Order> findById(Long id);

    Order save(Order order);

    void deleteAll();
}
