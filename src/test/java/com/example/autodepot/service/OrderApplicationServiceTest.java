package com.example.autodepot.service;

import com.example.autodepot.dto.OrderDTO;
import com.example.autodepot.entity.Order;
import com.example.autodepot.exception.BadRequestException;
import com.example.autodepot.mapper.OrderMapper;
import com.example.autodepot.service.data.OrderService;
import com.example.autodepot.service.impl.OrderApplicationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderApplicationServiceTest {

    @Mock
    private OrderService orderService;
    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderApplicationServiceImpl orderApplicationService;

    @Test
    void createOrder_WhenValidDto_CallsMapperAndSave() {
        OrderDTO dto = new OrderDTO("Berlin", "STANDARD", 1000.0);
        Order entity = new Order("Berlin", "STANDARD", 1000.0);
        when(orderMapper.toEntity(dto)).thenReturn(entity);
        when(orderService.save(any(Order.class))).thenReturn(entity);

        orderApplicationService.createOrder(dto);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderService).save(captor.capture());
        Order savedOrder = captor.getValue();
        assertTrue("Berlin".equals(savedOrder.getDestination())
            && "STANDARD".equals(savedOrder.getCargoType())
            && savedOrder.getWeight() == 1000.0);
    }

    @Test
    void createOrder_WhenDestinationEmpty_ThrowsBadRequest() {
        OrderDTO dto = new OrderDTO("", "FRAGILE", 500.0);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> orderApplicationService.createOrder(dto));
        assertTrue(ex.getMessage().contains("Destination"));
    }

    @Test
    void createOrder_WhenWeightInvalid_ThrowsBadRequest() {
        OrderDTO dto = new OrderDTO("Berlin", "STANDARD", -1.0);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> orderApplicationService.createOrder(dto));
        assertTrue(ex.getMessage().contains("Weight"));
    }
}
