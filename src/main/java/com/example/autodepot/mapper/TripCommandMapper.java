package com.example.autodepot.mapper;

import com.example.autodepot.dto.TripAssignDTO;
import com.example.autodepot.dto.TripBreakdownDTO;
import com.example.autodepot.dto.TripCompleteDTO;
import com.example.autodepot.dto.TripRepairDTO;
import com.example.autodepot.entity.Trip;
import com.example.autodepot.service.command.TripAssignCommand;
import com.example.autodepot.service.command.TripBreakdownCommand;
import com.example.autodepot.service.command.TripCompleteCommand;
import com.example.autodepot.service.command.TripRepairCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TripCommandMapper {
    TripAssignCommand toCommand(TripAssignDTO dto);
    TripCompleteCommand toCommand(TripCompleteDTO dto);
    TripBreakdownCommand toCommand(TripBreakdownDTO dto);
    TripRepairCommand toCommand(TripRepairDTO dto);

    @Mapping(target = "tripId", source = "id")
    TripBreakdownDTO toBreakdownDto(Trip trip);

    @Mapping(target = "tripId", source = "tripId")
    TripBreakdownDTO toBreakdownDto(Long tripId);

    @Mapping(target = "tripId", source = "tripId")
    TripRepairDTO toRepairDto(Long tripId);
}
