package com.example.autodepot.service;

import com.example.autodepot.dto.OrderViewDTO;
import com.example.autodepot.dto.TripViewDTO;

import java.util.List;

public interface FleetDashboardService {

    List<OrderViewDTO> getPendingOrders();

    List<TripViewDTO> getActiveTrips();
}
