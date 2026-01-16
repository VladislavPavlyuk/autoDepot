package com.example.autodepot.service;

import com.example.autodepot.entity.Order;
import com.example.autodepot.service.generation.OrderCountStrategy;
import com.example.autodepot.service.generation.OrderGenerator;
import com.example.autodepot.service.data.OrderService;
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
    private OrderService orderService;

    @Mock
    private OrderGenerator orderGenerator;

    @Mock
    private OrderCountStrategy orderCountStrategy;

    @InjectMocks
    private OrderGenerationService orderGenerationService;

    @Test
    void testGenerateRandomOrder() {
        // Arrange
        when(orderGenerator.generate()).thenReturn(new Order("New York", "STANDARD", 1000.0));
        when(orderService.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        orderGenerationService.generateRandomOrder();

        // Assert
        verify(orderService, times(1)).save(any(Order.class));
    }

    @Test
    void testGenerateRandomOrder_ValidOrder() {
        // Arrange
        Order generatedOrder = new Order("Chicago", "FRAGILE", 2500.0);
        when(orderGenerator.generate()).thenReturn(generatedOrder);
        when(orderService.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            assertNotNull(order.getDestination());
            assertNotNull(order.getCargoType());
            assertTrue(order.getWeight() >= 500 && order.getWeight() <= 5000);
            return order;
        });

        // Act
        orderGenerationService.generateRandomOrder();

        // Assert
        verify(orderService, times(1)).save(any(Order.class));
    }
}
