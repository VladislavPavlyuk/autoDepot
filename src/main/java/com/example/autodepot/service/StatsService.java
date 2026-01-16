package com.example.autodepot.service;

import com.example.autodepot.dto.CargoByDestinationDTO;
import com.example.autodepot.dto.DriverEarningsDTO;
import com.example.autodepot.dto.DriverPerformanceDTO;
import com.example.autodepot.dto.StatsSummaryDTO;
import com.example.autodepot.mapper.StatsMapper;
import com.example.autodepot.service.stats.StatsAggregator;
import com.example.autodepot.service.stats.StatsKey;
import com.example.autodepot.service.stats.StatsKeyRegistry;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class StatsService {
    private final StatsKeyRegistry statsKeyRegistry;
    private final StatsMapper statsMapper;

    public StatsService(StatsKeyRegistry statsKeyRegistry, StatsMapper statsMapper) {
        this.statsKeyRegistry = statsKeyRegistry;
        this.statsMapper = statsMapper;
    }

    public List<DriverPerformanceDTO> getDriverPerformance() {
        Map<String, Object> performance = getStatMap(StatsKey.DRIVER_PERFORMANCE);
        Map<String, Double> earnings = getStatMap(StatsKey.DRIVER_EARNINGS);
        return statsMapper.toDriverPerformanceList(performance, earnings);
    }

    public List<CargoByDestinationDTO> getCargoByDestination() {
        Map<String, Long> cargoByDestination = getStatMap(StatsKey.CARGO_BY_DESTINATION);
        return statsMapper.toCargoByDestinationList(cargoByDestination);
    }

    public List<DriverEarningsDTO> getDriverEarnings() {
        Map<String, Double> earnings = getStatMap(StatsKey.DRIVER_EARNINGS);
        return statsMapper.toDriverEarningsList(earnings);
    }

    public String getMostProfitable() {
        return getStatString(StatsKey.MOST_PROFITABLE_DRIVER);
    }

    public StatsSummaryDTO getAllStats() {
        StatsSummaryDTO summary = new StatsSummaryDTO();
        summary.setMostProfitableDriver(getMostProfitable());
        summary.setDriverPerformance(getDriverPerformance());
        summary.setDriverEarnings(getDriverEarnings());
        summary.setCargoByDestination(getCargoByDestination());
        return summary;
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
