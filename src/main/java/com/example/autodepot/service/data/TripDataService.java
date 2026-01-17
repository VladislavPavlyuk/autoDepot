package com.example.autodepot.service.data;

import com.example.autodepot.entity.Trip;

import java.util.List;
import java.util.Optional;

public interface TripDataService {
    Optional<Trip> findById(Long id);

    Trip save(Trip trip);

    List<Trip> findAll();

    boolean existsByOrderId(Long orderId);

    List<Object[]> findStatsByDriver();
}
