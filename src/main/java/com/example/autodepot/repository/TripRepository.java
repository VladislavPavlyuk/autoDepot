package com.example.autodepot.repository;

import com.example.autodepot.entity.Trip;

import java.util.List;
import java.util.Optional;

public interface TripRepository {

    Optional<Trip> findById(Long id);

    List<Trip> findAll();

    Trip save(Trip trip);

    boolean existsByOrderId(Long orderId);

    List<Object[]> findStatsByDriver();

    void deleteAll();
}
