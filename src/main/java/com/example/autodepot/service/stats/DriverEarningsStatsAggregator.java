package com.example.autodepot.service.stats;

import com.example.autodepot.entity.Driver;
import com.example.autodepot.service.data.DriverService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DriverEarningsStatsAggregator implements StatsAggregator {
    private final DriverService driverService;

    public DriverEarningsStatsAggregator(DriverService driverService) {
        this.driverService = driverService;
    }

    @Override
    public StatsKey getKey() {
        return StatsKey.DRIVER_EARNINGS;
    }

    @Override
    public Object aggregate() {
        List<Driver> drivers = driverService.findAll();
        Map<String, Double> earnings = new HashMap<>();

        for (Driver driver : drivers) {
            earnings.put(driver.getName(), driver.getEarnings());
        }

        return earnings;
    }
}
