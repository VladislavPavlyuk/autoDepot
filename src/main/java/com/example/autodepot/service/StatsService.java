package com.example.autodepot.service;

import com.example.autodepot.entity.Driver;
import com.example.autodepot.entity.Trip;
import com.example.autodepot.repository.DriverRepository;
import com.example.autodepot.repository.TripRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatsService {
    private final TripRepository tripRepo;
    private final DriverRepository driverRepository;

    public StatsService(TripRepository tripRepo, DriverRepository driverRepository) {
        this.tripRepo = tripRepo;
        this.driverRepository = driverRepository;
    }

    public Map<String, Object> getDriverPerformance() {
        List<Object[]> stats = tripRepo.findStatsByDriver();
        Map<String, Object> performance = new HashMap<>();
        
        for (Object[] stat : stats) {
            String driverName = (String) stat[0];
            Long tripCount = (Long) stat[1];
            Double totalWeight = ((Number) stat[2]).doubleValue();
            
            Map<String, Object> driverStats = new HashMap<>();
            driverStats.put("tripCount", tripCount);
            driverStats.put("totalWeight", totalWeight);
            performance.put(driverName, driverStats);
        }
        
        return performance;
    }

    public Map<String, Long> getCargoByDestination() {
        List<Trip> completedTrips = tripRepo.findAll().stream()
            .filter(t -> t.getStatus() == Trip.TripStatus.COMPLETED)
            .collect(Collectors.toList());

        Map<String, Long> destinationStats = new HashMap<>();
        for (Trip trip : completedTrips) {
            String destination = trip.getOrder().getDestination();
            destinationStats.merge(destination, 1L, (a, b) -> a + b);
        }

        return destinationStats;
    }

    public Map<String, Double> getDriverEarnings() {
        List<Driver> drivers = driverRepository.findAll();
        Map<String, Double> earnings = new HashMap<>();
        
        for (Driver driver : drivers) {
            earnings.put(driver.getName(), driver.getEarnings());
        }
        
        return earnings;
    }

    public String getMostProfitable() {
        Map<String, Double> earnings = getDriverEarnings();
        
        if (earnings.isEmpty()) {
            return "No data available";
        }

        String mostProfitableDriver = earnings.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(e -> e.getKey() + " ($" + String.format("%.2f", e.getValue()) + ")")
            .orElse("No data available");

        return mostProfitableDriver;
    }

    public Map<String, Object> getAllStats() {
        Map<String, Object> allStats = new HashMap<>();
        allStats.put("driverPerformance", getDriverPerformance());
        allStats.put("cargoByDestination", getCargoByDestination());
        allStats.put("driverEarnings", getDriverEarnings());
        allStats.put("mostProfitableDriver", getMostProfitable());
        return allStats;
    }
}
