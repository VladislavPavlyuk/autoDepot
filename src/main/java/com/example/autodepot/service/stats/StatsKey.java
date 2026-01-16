package com.example.autodepot.service.stats;

public enum StatsKey {
    DRIVER_PERFORMANCE("driverPerformance"),
    CARGO_BY_DESTINATION("cargoByDestination"),
    DRIVER_EARNINGS("driverEarnings"),
    MOST_PROFITABLE_DRIVER("mostProfitableDriver");

    private final String responseKey;

    StatsKey(String responseKey) {
        this.responseKey = responseKey;
    }

    public String getResponseKey() {
        return responseKey;
    }
}
