package com.example.autodepot.service.stats;

public interface StatsAggregator {
    String getKey();
    Object aggregate();
}
