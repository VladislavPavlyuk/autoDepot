package com.example.autodepot.service;

import com.example.autodepot.dto.OrderDTO;
import com.example.autodepot.entity.Order;
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

        verify(orderMapper).toEntity(dto);
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderService).save(captor.capture());
        assertEquals("Berlin", captor.getValue().getDestination());
        assertEquals("STANDARD", captor.getValue().getCargoType());
        assertEquals(1000.0, captor.getValue().getWeight());
    }

    @Test
    void createOrder_WhenDtoWithNullDestination_CallsSaveWithMappedEntity() {
        OrderDTO dto = new OrderDTO(null, "FRAGILE", 500.0);
        Order entity = new Order();
        entity.setDestination(null);
        entity.setCargoType("FRAGILE");
        entity.setWeight(500.0);
        when(orderMapper.toEntity(dto)).thenReturn(entity);
        when(orderService.save(any(Order.class))).thenReturn(entity);

        orderApplicationService.createOrder(dto);

        verify(orderService).save(entity);
    }
}
