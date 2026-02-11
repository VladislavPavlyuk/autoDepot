package com.example.autodepot.repository;

import com.example.autodepot.AbstractPostgresTest;
import com.example.autodepot.entity.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrderRepositoryTest extends AbstractPostgresTest {

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
    }

    @Test
    void save_WhenOrderSaved_ReturnsEntityWithIdAndFields() {
        Order order = new Order("Berlin", "STANDARD", 1000.0);
        Order saved = orderRepository.save(order);

        boolean actualResult = saved.getId() != null
            && "Berlin".equals(saved.getDestination())
            && "STANDARD".equals(saved.getCargoType())
            && saved.getWeight() == 1000.0;
        boolean expectedResult = true;
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void findById_WhenOrderExists_ReturnsOrderWithDestination() {
        Order order = orderRepository.save(new Order("Paris", "FRAGILE", 500.0));

        Optional<Order> found = orderRepository.findById(order.getId());
        String actualResult = found.map(Order::getDestination).orElse(null);
        String expectedResult = "Paris";
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void findById_WhenOrderNotExists_ReturnsEmpty() {
        Optional<Order> found = orderRepository.findById(999999L);
        boolean actualResult = found.isEmpty();
        boolean expectedResult = true;
        assertEquals(expectedResult, actualResult);
    }
}
