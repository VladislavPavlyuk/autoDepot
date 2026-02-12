package com.example.autodepot.service.impl;

import com.example.autodepot.dto.*;
import com.example.autodepot.entity.Driver;
import com.example.autodepot.service.ActivityLogService;
import com.example.autodepot.service.DashboardApiService;
import com.example.autodepot.service.FleetDashboardService;
import com.example.autodepot.service.StatsService;
import com.example.autodepot.service.data.DriverService;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DashboardApiServiceImpl implements DashboardApiService {

    private static final int ACTIVITY_LIMIT = 12;
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.GERMANY);
    private static final NumberFormat WEIGHT_FORMAT = NumberFormat.getNumberInstance(Locale.UK);

    static {
        WEIGHT_FORMAT.setMaximumFractionDigits(0);
        WEIGHT_FORMAT.setGroupingUsed(true);
    }

    private final StatsService statsService;
    private final FleetDashboardService fleetDashboardService;
    private final DriverService driverService;
    private final ActivityLogService activityLogService;

    public DashboardApiServiceImpl(StatsService statsService,
                                   FleetDashboardService fleetDashboardService,
                                   DriverService driverService,
                                   ActivityLogService activityLogService) {
        this.statsService = statsService;
        this.fleetDashboardService = fleetDashboardService;
        this.driverService = driverService;
        this.activityLogService = activityLogService;
    }

    @Override
    public DashboardResponseDTO getDashboardResponse() {
        StatsSummaryDTO statsSummary = statsService.getAllStats();
        List<OrderViewDTO> pendingOrders = fleetDashboardService.getPendingOrders();
        List<TripViewDTO> activeTrips = fleetDashboardService.getActiveTrips();

        DashboardResponseDTO response = new DashboardResponseDTO();
        response.setStats(buildStats(statsSummary, pendingOrders, activeTrips));
        response.setOrders(mapOrders(pendingOrders));
        response.setTrips(mapTrips(activeTrips));
        response.setActivity(activityLogService.readRecentActivity(ACTIVITY_LIMIT));
        response.setDriverPerformance(
            mergeDriverPerformance(driverService.findAll(), statsSummary.getDriverPerformance())
        );
        return response;
    }

    private List<DashboardStatDTO> buildStats(StatsSummaryDTO statsSummary,
                                              List<OrderViewDTO> pendingOrders,
                                              List<TripViewDTO> activeTrips) {
        List<DashboardStatDTO> stats = new ArrayList<>();
        stats.add(stat("activeTrips", String.valueOf(activeTrips.size())));
        stats.add(stat("pendingOrders", String.valueOf(pendingOrders.size())));

        String topDriver = statsSummary.getMostProfitableDriver();
        stats.add(stat("topDriver", topDriver == null || topDriver.isBlank() ? "—" : topDriver));

        int destinations = statsSummary.getCargoByDestination() == null
            ? 0
            : statsSummary.getCargoByDestination().size();
        stats.add(stat("destinations", String.valueOf(destinations)));
        return stats;
    }

    private DashboardStatDTO stat(String label, String value) {
        DashboardStatDTO dto = new DashboardStatDTO();
        dto.setLabel(label);
        dto.setValue(value);
        dto.setTrend("");
        return dto;
    }

    private List<DashboardOrderDTO> mapOrders(List<OrderViewDTO> orders) {
        return orders.stream().map(order -> {
            DashboardOrderDTO dto = new DashboardOrderDTO();
            dto.setOrderId(order.getId());
            dto.setId(formatOrderId(order.getId()));
            dto.setCargo(order.getCargoType());
            dto.setDestination(order.getDestination());
            dto.setWeight(formatWeight(order.getWeight()));
            dto.setStatus("QUEUED");
            return dto;
        }).collect(Collectors.toList());
    }

    private List<DashboardTripDTO> mapTrips(List<TripViewDTO> trips) {
        return trips.stream().map(trip -> {
            DashboardTripDTO dto = new DashboardTripDTO();
            dto.setTripId(trip.getId());
            dto.setId(formatTripId(trip.getId()));
            dto.setDriver(trip.getDriverName());
            dto.setCar(formatCarId(trip.getCarId()));
            dto.setStatus(formatTripStatus(trip.getStatus()));
            dto.setPayment(formatPayment(trip.getPayment()));
            return dto;
        }).collect(Collectors.toList());
    }

    private List<DriverPerformanceDTO> mergeDriverPerformance(List<Driver> drivers,
                                                               List<DriverPerformanceDTO> stats) {
        Map<String, DriverPerformanceDTO> statsByName = stats == null
            ? Map.of()
            : stats.stream().collect(Collectors.toMap(
                DriverPerformanceDTO::getDriverName,
                Function.identity(),
                (left, right) -> left
            ));

        List<DriverPerformanceDTO> merged = new ArrayList<>();
        for (Driver driver : drivers) {
            DriverPerformanceDTO dto = new DriverPerformanceDTO();
            dto.setDriverId(driver.getId());
            dto.setDriverName(driver.getName());
            dto.setLicenseYear(driver.getLicenseYear());
            DriverPerformanceDTO existing = statsByName.get(driver.getName());
            dto.setTripCount(existing == null ? 0L : existing.getTripCount());
            dto.setTotalWeight(existing == null ? 0.0 : existing.getTotalWeight());
            dto.setEarnings(driver.getEarnings());
            dto.setLicenseCategories(driver.getLicenseCategories() != null
                ? new ArrayList<>(driver.getLicenseCategories())
                : new ArrayList<>());
            dto.setExperience(driver.getExperience());
            merged.add(dto);
        }

        merged.sort(Comparator.comparing(DriverPerformanceDTO::getDriverName));
        return merged;
    }

    private String formatOrderId(Long id) {
        return id == null ? "ORD-?" : "ORD-" + id;
    }

    private String formatTripId(Long id) {
        return id == null ? "TR-?" : "TR-" + id;
    }

    private String formatCarId(Long id) {
        return id == null ? "C-?" : "C-" + id;
    }

    private String formatTripStatus(String status) {
        if (status == null) {
            return "IN_PROGRESS";
        }
        return switch (status) {
            case "IN_PROGRESS", "BROKEN", "REPAIR_REQUESTED", "COMPLETED" -> status;
            default -> status;
        };
    }

    private String formatPayment(Double payment) {
        if (payment == null) {
            return "€0.00";
        }
        return CURRENCY_FORMAT.format(payment);
    }

    private String formatWeight(double weight) {
        return WEIGHT_FORMAT.format(weight) + " kg";
    }
}
