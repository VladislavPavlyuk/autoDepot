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
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void generateRandomOrder_WhenCalled_SavesGeneratedOrder() {
        Order generatedOrder = new Order("Berlin", "STANDARD", 1000.0);
        when(orderGenerator.generate()).thenReturn(generatedOrder);
        when(orderService.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderGenerationService.generateRandomOrder();

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderService, times(1)).save(orderCaptor.capture());
        Order actualOrder = orderCaptor.getValue();
        Order expectedOrder = generatedOrder;

        boolean actualResult = actualOrder == expectedOrder;
        boolean expectedResult = true;
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void generateRandomOrder_WhenGeneratedOrderValid_SavesOrderWithValidFields() {
        Order generatedOrder = new Order("Madrid", "FRAGILE", 2500.0);
        when(orderGenerator.generate()).thenReturn(generatedOrder);
        when(orderService.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderGenerationService.generateRandomOrder();

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderService, times(1)).save(orderCaptor.capture());
        Order actualOrder = orderCaptor.getValue();

        boolean actualResult = actualOrder.getDestination() != null
            && actualOrder.getCargoType() != null
            && actualOrder.getWeight() >= 500
            && actualOrder.getWeight() <= 5000;
        boolean expectedResult = true;
        assertEquals(expectedResult, actualResult);
    }
}
