package com.example.autodepot.service;

import com.example.autodepot.dto.TripAssignDTO;
import com.example.autodepot.dto.TripBreakdownDTO;
import com.example.autodepot.dto.TripCompleteDTO;
import com.example.autodepot.dto.TripRepairDTO;

public interface TripService {

    void createTrip(TripAssignDTO assignDTO);

    void processBreakdown(TripBreakdownDTO breakdownDTO);

    void processBreakdown(Long tripId);

    void requestRepair(TripRepairDTO repairDTO);

    void requestRepair(Long tripId);

    void confirmRepairComplete(TripRepairDTO repairDTO);

    void confirmRepairComplete(Long tripId);

    void completeTrip(TripCompleteDTO completeDTO);

    void simulateRandomBreakdown();
}
