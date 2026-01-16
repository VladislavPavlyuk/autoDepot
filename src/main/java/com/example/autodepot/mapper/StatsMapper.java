package com.example.autodepot.mapper;

import com.example.autodepot.dto.CargoByDestinationDTO;
import com.example.autodepot.dto.DriverEarningsDTO;
import com.example.autodepot.dto.DriverPerformanceDTO;
import org.mapstruct.Mapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface StatsMapper {
    default List<DriverPerformanceDTO> toDriverPerformanceList(Map<String, Object> performance,
                                                               Map<String, Double> earnings) {
        List<DriverPerformanceDTO> result = new ArrayList<>();
        for (Map.Entry<String, Object> entry : performance.entrySet()) {
            if (!(entry.getValue() instanceof Map<?, ?> statsMap)) {
                continue;
            }
            Object tripCountValue = statsMap.get("tripCount");
            Object totalWeightValue = statsMap.get("totalWeight");

            DriverPerformanceDTO dto = new DriverPerformanceDTO();
            dto.setDriverName(entry.getKey());
            dto.setTripCount(tripCountValue == null ? 0L : ((Number) tripCountValue).longValue());
            dto.setTotalWeight(totalWeightValue == null ? 0.0 : ((Number) totalWeightValue).doubleValue());
            dto.setEarnings(earnings.getOrDefault(entry.getKey(), 0.0));
            result.add(dto);
        }
        result.sort(Comparator.comparing(DriverPerformanceDTO::getDriverName));
        return result;
    }

    default List<DriverEarningsDTO> toDriverEarningsList(Map<String, Double> earnings) {
        List<DriverEarningsDTO> result = new ArrayList<>();
        for (Map.Entry<String, Double> entry : earnings.entrySet()) {
            DriverEarningsDTO dto = new DriverEarningsDTO();
            dto.setDriverName(entry.getKey());
            dto.setEarnings(entry.getValue() == null ? 0.0 : entry.getValue());
            result.add(dto);
        }
        result.sort(Comparator.comparing(DriverEarningsDTO::getDriverName));
        return result;
    }

    default List<CargoByDestinationDTO> toCargoByDestinationList(Map<String, Long> cargoByDestination) {
        List<CargoByDestinationDTO> result = new ArrayList<>();
        for (Map.Entry<String, Long> entry : cargoByDestination.entrySet()) {
            CargoByDestinationDTO dto = new CargoByDestinationDTO();
            dto.setDestination(entry.getKey());
            dto.setCargoCount(entry.getValue() == null ? 0L : entry.getValue());
            result.add(dto);
        }
        result.sort(Comparator.comparing(CargoByDestinationDTO::getDestination));
        return result;
    }
}
