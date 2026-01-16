package com.example.autodepot.service.data;

import com.example.autodepot.entity.Trip;
import com.example.autodepot.repository.TripRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TripDataService {
    private final TripRepository tripRepository;

    public TripDataService(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    public Optional<Trip> findById(Long id) {
        return tripRepository.findById(id);
    }

    public Trip save(Trip trip) {
        return tripRepository.save(trip);
    }

    public List<Trip> findAll() {
        return tripRepository.findAll();
    }

    public boolean existsByOrderId(Long orderId) {
        return tripRepository.existsByOrderId(orderId);
    }

    public List<Object[]> findStatsByDriver() {
        return tripRepository.findStatsByDriver();
    }
}
