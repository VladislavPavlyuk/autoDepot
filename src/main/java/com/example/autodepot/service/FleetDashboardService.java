package com.example.autodepot.service;

import com.example.autodepot.dto.OrderViewDTO;
import com.example.autodepot.dto.TripViewDTO;
import com.example.autodepot.entity.Trip;
import com.example.autodepot.mapper.OrderMapper;
import com.example.autodepot.mapper.TripMapper;
import com.example.autodepot.service.data.OrderService;
import com.example.autodepot.service.data.TripDataService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FleetDashboardService {
    private final OrderService orderService;
    private final TripDataService tripDataService;
    private final OrderMapper orderMapper;
    private final TripMapper tripMapper;

    public FleetDashboardService(OrderService orderService,
                                 TripDataService tripDataService,
                                 OrderMapper orderMapper,
                                 TripMapper tripMapper) {
        this.orderService = orderService;
        this.tripDataService = tripDataService;
        this.orderMapper = orderMapper;
        this.tripMapper = tripMapper;
    }

    public List<OrderViewDTO> getPendingOrders() {
        return orderService.findAll().stream()
            .filter(order -> !tripDataService.existsByOrderId(order.getId()))
            .map(orderMapper::toViewDto)
            .collect(Collectors.toList());
    }

    public List<TripViewDTO> getActiveTrips() {
        return tripDataService.findAll().stream()
            .filter(trip -> trip.getStatus() == Trip.TripStatus.IN_PROGRESS
                || trip.getStatus() == Trip.TripStatus.BROKEN
                || trip.getStatus() == Trip.TripStatus.REPAIR_REQUESTED)
            .map(tripMapper::toViewDto)
            .collect(Collectors.toList());
    }
}
