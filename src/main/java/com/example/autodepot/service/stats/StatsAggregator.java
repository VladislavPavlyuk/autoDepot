package com.example.autodepot.service.stats;

public interface StatsAggregator {
    StatsKey getKey();
    Object aggregate();
}
