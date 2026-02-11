package com.example.autodepot.service;

import com.example.autodepot.dto.TripAssignDTO;
import com.example.autodepot.dto.TripBreakdownDTO;
import com.example.autodepot.dto.TripCompleteDTO;
import com.example.autodepot.dto.TripRepairDTO;

public interface TripService {

    void createTrip(TripAssignDTO assignDTO);

    void processBreakdown(TripBreakdownDTO breakdownDTO);

    void requestRepair(TripRepairDTO repairDTO);

    void completeTrip(TripCompleteDTO completeDTO);

    void simulateRandomBreakdown();
}
