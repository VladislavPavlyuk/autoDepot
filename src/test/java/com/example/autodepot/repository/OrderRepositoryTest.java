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
    void save_WhenOrderSaved_ReturnsEntityWithId() {
        Order order = new Order("Berlin", "STANDARD", 1000.0);
        Order saved = orderRepository.save(order);

        assertNotNull(saved.getId());
        assertEquals("Berlin", saved.getDestination());
        assertEquals("STANDARD", saved.getCargoType());
        assertEquals(1000.0, saved.getWeight());
    }

    @Test
    void findById_WhenOrderExists_ReturnsOrder() {
        Order order = orderRepository.save(new Order("Paris", "FRAGILE", 500.0));

        Optional<Order> found = orderRepository.findById(order.getId());

        assertTrue(found.isPresent());
        assertEquals("Paris", found.get().getDestination());
    }

    @Test
    void findById_WhenOrderNotExists_ReturnsEmpty() {
        Optional<Order> found = orderRepository.findById(999999L);
        assertTrue(found.isEmpty());
    }
}
