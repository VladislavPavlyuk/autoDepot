package com.example.autodepot.service;

import com.example.autodepot.service.stats.StatsAggregator;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsService {
    private final Map<String, StatsAggregator> aggregatorsByKey;

    public StatsService(List<StatsAggregator> aggregators) {
        this.aggregatorsByKey = new HashMap<>();
        for (StatsAggregator aggregator : aggregators) {
            String key = aggregator.getKey();
            if (aggregatorsByKey.containsKey(key)) {
                throw new IllegalStateException("Duplicate stats aggregator key: " + key);
            }
            aggregatorsByKey.put(key, aggregator);
        }
    }

    public Map<String, Object> getDriverPerformance() {
        return getStatMap("driverPerformance");
    }

    public Map<String, Long> getCargoByDestination() {
        return getStatMap("cargoByDestination");
    }

    public Map<String, Double> getDriverEarnings() {
        return getStatMap("driverEarnings");
    }

    public String getMostProfitable() {
        return getStatString("mostProfitableDriver");
    }

    public Map<String, Object> getAllStats() {
        Map<String, Object> allStats = new HashMap<>();
        for (StatsAggregator aggregator : aggregatorsByKey.values()) {
            allStats.put(aggregator.getKey(), aggregator.aggregate());
        }
        return allStats;
    }

    private String getStatString(String key) {
        return getStat(key, String.class);
    }

    @SuppressWarnings("unchecked")
    private <K, V> Map<K, V> getStatMap(String key) {
        return (Map<K, V>) getStat(key, Map.class);
    }

    private <T> T getStat(String key, Class<T> type) {
        StatsAggregator aggregator = aggregatorsByKey.get(key);
        if (aggregator == null) {
            throw new IllegalStateException("Missing stats aggregator: " + key);
        }

        Object value = aggregator.aggregate();
        if (!type.isInstance(value)) {
            throw new IllegalStateException("Stats type mismatch for key: " + key);
        }

        return type.cast(value);
    }
}
