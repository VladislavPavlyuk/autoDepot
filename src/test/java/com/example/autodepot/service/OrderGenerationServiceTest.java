package com.example.autodepot.service;

import com.example.autodepot.entity.Order;
import com.example.autodepot.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderGenerationServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderGenerationService orderGenerationService;

    @Test
    void testGenerateRandomOrder() {
        // Arrange
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        orderGenerationService.generateRandomOrder();

        // Assert
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testGenerateRandomOrder_ValidOrder() {
        // Arrange
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            assertNotNull(order.getDestination());
            assertNotNull(order.getCargoType());
            assertTrue(order.getWeight() >= 500 && order.getWeight() <= 5000);
            return order;
        });

        // Act
        orderGenerationService.generateRandomOrder();

        // Assert
        verify(orderRepository, times(1)).save(any(Order.class));
    }
}
