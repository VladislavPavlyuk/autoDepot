package com.example.autodepot.service.stats;

import com.example.autodepot.repository.TripRepository;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DriverPerformanceStatsAggregator implements StatsAggregator {
    private final TripRepository tripRepo;

    public DriverPerformanceStatsAggregator(TripRepository tripRepo) {
        this.tripRepo = tripRepo;
    }

    @Override
    public String getKey() {
        return "driverPerformance";
    }

    @Override
    public Object aggregate() {
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
}
