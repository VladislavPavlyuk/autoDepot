package com.example.autodepot.mapper;

import com.example.autodepot.dto.TripViewDTO;
import com.example.autodepot.entity.Trip;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TripMapper {
    @Mapping(target = "driverName", source = "driver.name")
    @Mapping(target = "carId", source = "car.id")
    @Mapping(target = "carCapacity", source = "car.capacity")
    @Mapping(target = "destination", source = "order.destination")
    @Mapping(target = "cargoType", source = "order.cargoType")
    @Mapping(target = "weight", source = "order.weight")
    @Mapping(target = "status", source = "status")
    TripViewDTO toViewDto(Trip trip);
}
