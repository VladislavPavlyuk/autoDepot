package com.example.autodepot.controller;

import com.example.autodepot.dto.DashboardOrderDTO;
import com.example.autodepot.dto.DashboardResponseDTO;
import com.example.autodepot.dto.DashboardStatDTO;
import com.example.autodepot.dto.DashboardTripDTO;
import com.example.autodepot.dto.OrderDTO;
import com.example.autodepot.dto.OrderViewDTO;
import com.example.autodepot.dto.StatsSummaryDTO;
import com.example.autodepot.dto.TripAssignDTO;
import com.example.autodepot.dto.TripBreakdownDTO;
import com.example.autodepot.dto.TripCompleteDTO;
import com.example.autodepot.dto.TripRepairDTO;
import com.example.autodepot.dto.TripViewDTO;
import com.example.autodepot.mapper.TripCommandMapper;
import com.example.autodepot.service.FleetDashboardService;
import com.example.autodepot.service.OrderApplicationService;
import com.example.autodepot.service.OrderGenerationService;
import com.example.autodepot.service.StatsService;
import com.example.autodepot.service.TripService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class FleetApiController {
    private static final String LOG_FILE = "trips.log";
    private static final int ACTIVITY_LIMIT = 12;
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);
    private static final NumberFormat WEIGHT_FORMAT = NumberFormat.getNumberInstance(Locale.US);

    static {
        WEIGHT_FORMAT.setMaximumFractionDigits(0);
        WEIGHT_FORMAT.setGroupingUsed(true);
    }

    private final TripService tripService;
    private final StatsService statsService;
    private final FleetDashboardService fleetDashboardService;
    private final OrderApplicationService orderApplicationService;
    private final OrderGenerationService orderGenerationService;
    private final TripCommandMapper tripCommandMapper;

    public FleetApiController(TripService tripService,
                              StatsService statsService,
                              FleetDashboardService fleetDashboardService,
                              OrderApplicationService orderApplicationService,
                              OrderGenerationService orderGenerationService,
                              TripCommandMapper tripCommandMapper) {
        this.tripService = tripService;
        this.statsService = statsService;
        this.fleetDashboardService = fleetDashboardService;
        this.orderApplicationService = orderApplicationService;
        this.orderGenerationService = orderGenerationService;
        this.tripCommandMapper = tripCommandMapper;
    }

    @GetMapping("/dashboard")
    public DashboardResponseDTO getDashboard() {
        StatsSummaryDTO statsSummary = statsService.getAllStats();
        List<OrderViewDTO> pendingOrders = fleetDashboardService.getPendingOrders();
        List<TripViewDTO> activeTrips = fleetDashboardService.getActiveTrips();

        DashboardResponseDTO response = new DashboardResponseDTO();
        response.setStats(buildStats(statsSummary, pendingOrders, activeTrips));
        response.setOrders(mapOrders(pendingOrders));
        response.setTrips(mapTrips(activeTrips));
        response.setActivity(readActivity());
        return response;
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createOrder(@RequestBody OrderDTO orderDTO) {
        orderApplicationService.createOrder(orderDTO);
    }

    @PostMapping("/orders/generate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void generateRandomOrder() {
        orderGenerationService.generateRandomOrder();
    }

    @PostMapping("/trips/assign")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void assignTrip(@RequestBody TripAssignDTO tripAssignDTO) {
        tripService.createTrip(tripAssignDTO);
    }

    @PostMapping("/trips/{tripId}/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void completeTrip(@PathVariable Long tripId, @RequestBody TripCompleteDTO tripCompleteDTO) {
        tripCompleteDTO.setTripId(tripId);
        tripService.completeTrip(tripCompleteDTO);
    }

    @PostMapping("/trips/{tripId}/breakdown")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reportBreakdown(@PathVariable Long tripId) {
        TripBreakdownDTO breakdownDTO = tripCommandMapper.toBreakdownDto(tripId);
        tripService.processBreakdown(breakdownDTO);
    }

    @PostMapping("/trips/{tripId}/repair")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void requestRepair(@PathVariable Long tripId) {
        TripRepairDTO repairDTO = tripCommandMapper.toRepairDto(tripId);
        tripService.requestRepair(repairDTO);
    }

    @PostMapping("/trips/simulate-breakdown")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void simulateBreakdown() {
        tripService.simulateRandomBreakdown();
    }

    private List<DashboardStatDTO> buildStats(StatsSummaryDTO statsSummary,
                                              List<OrderViewDTO> pendingOrders,
                                              List<TripViewDTO> activeTrips) {
        List<DashboardStatDTO> stats = new ArrayList<>();
        stats.add(stat("Active trips", String.valueOf(activeTrips.size())));
        stats.add(stat("Pending orders", String.valueOf(pendingOrders.size())));

        String topDriver = statsSummary.getMostProfitableDriver();
        stats.add(stat("Top driver", topDriver == null || topDriver.isBlank() ? "N/A" : topDriver));

        int destinations = statsSummary.getCargoByDestination() == null
            ? 0
            : statsSummary.getCargoByDestination().size();
        stats.add(stat("Destinations", String.valueOf(destinations)));
        return stats;
    }

    private DashboardStatDTO stat(String label, String value) {
        DashboardStatDTO stat = new DashboardStatDTO();
        stat.setLabel(label);
        stat.setValue(value);
        stat.setTrend("N/A");
        return stat;
    }

    private List<DashboardOrderDTO> mapOrders(List<OrderViewDTO> orders) {
        return orders.stream().map(order -> {
            DashboardOrderDTO dto = new DashboardOrderDTO();
            dto.setOrderId(order.getId());
            dto.setId(formatOrderId(order.getId()));
            dto.setCargo(order.getCargoType());
            dto.setDestination(order.getDestination());
            dto.setWeight(formatWeight(order.getWeight()));
            dto.setStatus("Queued");
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

    private List<String> readActivity() {
        Path logPath = Paths.get(LOG_FILE);
        if (!Files.exists(logPath)) {
            return List.of();
        }
        try {
            List<String> lines = Files.readAllLines(logPath).stream()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .collect(Collectors.toList());
            int start = Math.max(0, lines.size() - ACTIVITY_LIMIT);
            return lines.subList(start, lines.size());
        } catch (IOException e) {
            return List.of();
        }
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
            return "Unknown";
        }
        return switch (status) {
            case "IN_PROGRESS" -> "In progress";
            case "BROKEN" -> "Broken";
            case "REPAIR_REQUESTED" -> "Repair Requested";
            case "COMPLETED" -> "Completed";
            default -> status;
        };
    }

    private String formatPayment(Double payment) {
        if (payment == null) {
            return "$0.00";
        }
        return CURRENCY_FORMAT.format(payment);
    }

    private String formatWeight(double weight) {
        return WEIGHT_FORMAT.format(weight) + " kg";
    }
}
