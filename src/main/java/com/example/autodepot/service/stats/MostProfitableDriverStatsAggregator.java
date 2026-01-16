package com.example.autodepot.service.stats;

import com.example.autodepot.entity.Driver;
import com.example.autodepot.repository.DriverRepository;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class MostProfitableDriverStatsAggregator implements StatsAggregator {
    private final DriverRepository driverRepository;

    public MostProfitableDriverStatsAggregator(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    @Override
    public StatsKey getKey() {
        return StatsKey.MOST_PROFITABLE_DRIVER;
    }

    @Override
    public Object aggregate() {
        List<Driver> drivers = driverRepository.findAll();
        if (drivers.isEmpty()) {
            return "No data available";
        }

        Optional<Driver> mostProfitable = drivers.stream()
            .max(Comparator.comparing(Driver::getEarnings));

        return mostProfitable
            .map(d -> d.getName() + " ($" + String.format("%.2f", d.getEarnings()) + ")")
            .orElse("No data available");
    }
}
