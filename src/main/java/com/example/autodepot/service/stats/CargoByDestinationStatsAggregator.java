package com.example.autodepot.service.stats;

import com.example.autodepot.entity.Trip;
import com.example.autodepot.service.data.TripDataService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CargoByDestinationStatsAggregator implements StatsAggregator {
    private final TripDataService tripDataService;

    public CargoByDestinationStatsAggregator(TripDataService tripDataService) {
        this.tripDataService = tripDataService;
    }

    @Override
    public StatsKey getKey() {
        return StatsKey.CARGO_BY_DESTINATION;
    }

    @Override
    public Object aggregate() {
        List<Trip> completedTrips = tripDataService.findAll().stream()
            .filter(t -> t.getStatus() == Trip.TripStatus.COMPLETED)
            .collect(Collectors.toList());

        Map<String, Long> destinationStats = new HashMap<>();
        for (Trip trip : completedTrips) {
            String destination = trip.getOrder().getDestination();
            destinationStats.merge(destination, 1L, (a, b) -> a + b);
        }

        return destinationStats;
    }
}
