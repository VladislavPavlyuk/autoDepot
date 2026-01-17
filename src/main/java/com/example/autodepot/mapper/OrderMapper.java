package com.example.autodepot.mapper;

import com.example.autodepot.dto.OrderDTO;
import com.example.autodepot.dto.OrderViewDTO;
import com.example.autodepot.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "trips", ignore = true)
    Order toEntity(OrderDTO dto);
    OrderViewDTO toViewDto(Order order);
}
