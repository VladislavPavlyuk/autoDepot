package com.example.autodepot.service.stats;

import com.example.autodepot.service.data.TripDataService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DriverPerformanceStatsAggregator implements StatsAggregator {
    private final TripDataService tripDataService;

    public DriverPerformanceStatsAggregator(TripDataService tripDataService) {
        this.tripDataService = tripDataService;
    }

    @Override
    public StatsKey getKey() {
        return StatsKey.DRIVER_PERFORMANCE;
    }

    @Override
    public Object aggregate() {
        List<Object[]> stats = tripDataService.findStatsByDriver();
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
