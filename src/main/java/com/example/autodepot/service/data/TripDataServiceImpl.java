package com.example.autodepot.service.data;

import com.example.autodepot.entity.Trip;
import com.example.autodepot.repository.TripRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TripDataServiceImpl implements TripDataService {
    private final TripRepository tripRepository;

    public TripDataServiceImpl(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    @Override
    public Optional<Trip> findById(Long id) {
        return tripRepository.findById(id);
    }

    @Override
    public Trip save(Trip trip) {
        return tripRepository.save(trip);
    }

    @Override
    public List<Trip> findAll() {
        return tripRepository.findAll();
    }

    @Override
    public boolean existsByOrderId(Long orderId) {
        return tripRepository.existsByOrderId(orderId);
    }

    @Override
    public List<Object[]> findStatsByDriver() {
        return tripRepository.findStatsByDriver();
    }
}
