package com.example.autodepot.service.stats;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class StatsKeyRegistry {
    private final Map<StatsKey, StatsAggregator> aggregatorsByKey;

    public StatsKeyRegistry(List<StatsAggregator> aggregators) {
        this.aggregatorsByKey = new EnumMap<>(StatsKey.class);
        for (StatsAggregator aggregator : aggregators) {
            StatsKey key = aggregator.getKey();
            if (aggregatorsByKey.containsKey(key)) {
                throw new IllegalStateException("Duplicate stats aggregator key: " + key);
            }
            aggregatorsByKey.put(key, aggregator);
        }
    }

    public StatsAggregator get(StatsKey key) {
        StatsAggregator aggregator = aggregatorsByKey.get(key);
        if (aggregator == null) {
            throw new IllegalStateException("Missing stats aggregator: " + key);
        }
        return aggregator;
    }

    public Map<StatsKey, StatsAggregator> getAll() {
        return aggregatorsByKey;
    }
}
