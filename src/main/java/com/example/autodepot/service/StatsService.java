package com.example.autodepot.service;

import com.example.autodepot.service.stats.StatsAggregator;
import com.example.autodepot.service.stats.StatsKey;
import com.example.autodepot.service.stats.StatsKeyRegistry;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StatsService {
    private final StatsKeyRegistry statsKeyRegistry;

    public StatsService(StatsKeyRegistry statsKeyRegistry) {
        this.statsKeyRegistry = statsKeyRegistry;
    }

    public Map<String, Object> getDriverPerformance() {
        return getStatMap(StatsKey.DRIVER_PERFORMANCE);
    }

    public Map<String, Long> getCargoByDestination() {
        return getStatMap(StatsKey.CARGO_BY_DESTINATION);
    }

    public Map<String, Double> getDriverEarnings() {
        return getStatMap(StatsKey.DRIVER_EARNINGS);
    }

    public String getMostProfitable() {
        return getStatString(StatsKey.MOST_PROFITABLE_DRIVER);
    }

    public Map<String, Object> getAllStats() {
        Map<String, Object> allStats = new HashMap<>();
        for (Map.Entry<StatsKey, StatsAggregator> entry : statsKeyRegistry.getAll().entrySet()) {
            allStats.put(entry.getKey().getResponseKey(), entry.getValue().aggregate());
        }
        return allStats;
    }

    private String getStatString(StatsKey key) {
        return getStat(key, String.class);
    }

    @SuppressWarnings("unchecked")
    private <K, V> Map<K, V> getStatMap(StatsKey key) {
        return (Map<K, V>) getStat(key, Map.class);
    }

    private <T> T getStat(StatsKey key, Class<T> type) {
        StatsAggregator aggregator = statsKeyRegistry.get(key);
        Object value = aggregator.aggregate();
        if (!type.isInstance(value)) {
            throw new IllegalStateException("Stats type mismatch for key: " + key);
        }

        return type.cast(value);
    }

}
