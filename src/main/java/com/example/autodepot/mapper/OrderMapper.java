package com.example.autodepot.mapper;

import com.example.autodepot.dto.OrderDTO;
import com.example.autodepot.dto.OrderViewDTO;
import com.example.autodepot.entity.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    Order toEntity(OrderDTO dto);
    OrderViewDTO toViewDto(Order order);
}
