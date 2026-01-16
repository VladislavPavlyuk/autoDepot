package com.example.autodepot.service.stats;

public enum StatsKey {
    DRIVER_PERFORMANCE("driverPerformance"),
    CARGO_BY_DESTINATION("cargoByDestination"),
    DRIVER_EARNINGS("driverEarnings"),
    MOST_PROFITABLE_DRIVER("mostProfitableDriver");

    private final String responseKey;

    private static final java.util.Map<String, StatsKey> BY_RESPONSE_KEY =
        java.util.Arrays.stream(values())
            .collect(java.util.stream.Collectors.toUnmodifiableMap(
                StatsKey::getResponseKey, key -> key));

    StatsKey(String responseKey) {
        this.responseKey = responseKey;
    }

    public String getResponseKey() {
        return responseKey;
    }

    public static StatsKey fromResponseKey(String responseKey) {
        StatsKey key = BY_RESPONSE_KEY.get(responseKey);
        if (key == null) {
            throw new IllegalArgumentException("Unknown stats response key: " + responseKey);
        }
        return key;
    }
}
