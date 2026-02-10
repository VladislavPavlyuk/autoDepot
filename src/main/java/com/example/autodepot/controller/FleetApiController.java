package com.example.autodepot.controller;

import com.example.autodepot.dto.DashboardOrderDTO;
import com.example.autodepot.dto.DashboardResponseDTO;
import com.example.autodepot.dto.DashboardStatDTO;
import com.example.autodepot.dto.DashboardTripDTO;
import com.example.autodepot.dto.DriverCreateDTO;
import com.example.autodepot.dto.DriverPerformanceDTO;
import com.example.autodepot.dto.OrderDTO;
import com.example.autodepot.exception.BadRequestException;
import com.example.autodepot.dto.OrderViewDTO;
import com.example.autodepot.dto.StatsSummaryDTO;
import com.example.autodepot.dto.TripAssignDTO;
import com.example.autodepot.dto.TripBreakdownDTO;
import com.example.autodepot.dto.TripCompleteDTO;
import com.example.autodepot.dto.TripRepairDTO;
import com.example.autodepot.dto.TripViewDTO;
import com.example.autodepot.entity.Driver;
import com.example.autodepot.mapper.TripCommandMapper;
import com.example.autodepot.service.FleetDashboardService;
import com.example.autodepot.service.OrderApplicationService;
import com.example.autodepot.service.OrderGenerationService;
import com.example.autodepot.service.StatsService;
import com.example.autodepot.service.TripService;
import com.example.autodepot.service.data.DriverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class FleetApiController {
    private static final Logger log = LoggerFactory.getLogger(FleetApiController.class);
    private static final String LOG_FILE = "trips.log";
    private static final int ACTIVITY_LIMIT = 12;
    private static final int MAX_ORDER_STRING_LENGTH = 255;
    private static final double MAX_ORDER_WEIGHT_KG = 100_000.0;
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.GERMANY);
    private static final NumberFormat WEIGHT_FORMAT = NumberFormat.getNumberInstance(Locale.UK);

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
    private final DriverService driverService;
    private static final com.fasterxml.jackson.databind.ObjectMapper OBJECT_MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();

    public FleetApiController(TripService tripService,
                              StatsService statsService,
                              FleetDashboardService fleetDashboardService,
                              OrderApplicationService orderApplicationService,
                              OrderGenerationService orderGenerationService,
                              TripCommandMapper tripCommandMapper,
                              DriverService driverService) {
        this.tripService = tripService;
        this.statsService = statsService;
        this.fleetDashboardService = fleetDashboardService;
        this.orderApplicationService = orderApplicationService;
        this.orderGenerationService = orderGenerationService;
        this.tripCommandMapper = tripCommandMapper;
        this.driverService = driverService;
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
        response.setDriverPerformance(
            mergeDriverPerformance(driverService.findAll(), statsSummary.getDriverPerformance())
        );
        return response;
    }

    @PostMapping(value = "/drivers", consumes = {"application/json", "application/json;charset=UTF-8"})
    public ResponseEntity<Map<String, String>> createDriver(@RequestBody(required = false) byte[] rawBytes) {
        String rawBody = rawBytes == null || rawBytes.length == 0
            ? null
            : new String(rawBytes, java.nio.charset.StandardCharsets.UTF_8);
        if (rawBody == null || rawBody.isBlank()) {
            throw new BadRequestException("Request body is required. Send JSON: name, licenseYear, licenseCategories (array).");
        }
        DriverCreateDTO driverCreateDTO;
        try {
            driverCreateDTO = OBJECT_MAPPER.readValue(rawBody, DriverCreateDTO.class);
        } catch (IOException e) {
            throw new BadRequestException("Invalid JSON: " + (e.getMessage() != null ? e.getMessage() : "parse error"));
        }
        if (driverCreateDTO == null) {
            throw new BadRequestException("Driver payload is required");
        }
        String name = driverCreateDTO.getName() == null ? "" : driverCreateDTO.getName().trim();
        if (name.isEmpty()) {
            throw new BadRequestException("Driver name is required");
        }
        if (name.length() > MAX_ORDER_STRING_LENGTH) {
            throw new BadRequestException("Driver name must be at most " + MAX_ORDER_STRING_LENGTH + " characters");
        }
        List<String> categories = driverCreateDTO.getLicenseCategories();
        if (categories == null || categories.isEmpty()) {
            throw new BadRequestException("At least one license category (A–E) is required");
        }
        List<String> valid = new ArrayList<>();
        for (String c : categories) {
            String cat = c == null ? "" : c.trim().toUpperCase();
            if (cat.matches("^[A-E]$") && !valid.contains(cat)) {
                valid.add(cat);
            }
        }
        if (valid.isEmpty()) {
            throw new BadRequestException("Driver license categories must be one or more of A, B, C, D, E");
        }
        Integer licenseYear = driverCreateDTO.getLicenseYear();
        int currentYear = java.time.Year.now().getValue();
        if (licenseYear == null || licenseYear < 1970 || licenseYear > currentYear) {
            throw new BadRequestException("Driver license year must be between 1970 and current year");
        }
        Driver driver = new Driver(name, licenseYear);
        driver.setLicenseCategories(valid);
        driver.setAvailable(true);
        driver.setEarnings(0.0);
        driverService.save(driver);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "created"));
    }

    @PostMapping("/orders")
    public ResponseEntity<Map<String, String>> createOrder(@RequestBody OrderDTO orderDTO) {
        if (orderDTO == null) {
            throw new BadRequestException("Order payload is required");
        }
        String dest = orderDTO.getDestination() == null ? "" : orderDTO.getDestination().trim();
        String cargo = orderDTO.getCargoType() == null ? "" : orderDTO.getCargoType().trim();
        if (dest.isEmpty()) {
            throw new BadRequestException("Destination is required");
        }
        if (cargo.isEmpty()) {
            throw new BadRequestException("Cargo type is required");
        }
        if (dest.length() > MAX_ORDER_STRING_LENGTH || cargo.length() > MAX_ORDER_STRING_LENGTH) {
            throw new BadRequestException("Destination and cargo type must be at most " + MAX_ORDER_STRING_LENGTH + " characters");
        }
        double weight = orderDTO.getWeight();
        if (weight <= 0 || weight > MAX_ORDER_WEIGHT_KG) {
            throw new BadRequestException("Weight must be between 0 and " + (long) MAX_ORDER_WEIGHT_KG + " kg");
        }
        orderApplicationService.createOrder(orderDTO);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/orders/generate")
    public ResponseEntity<?> generateRandomOrder() {
        log.info("POST /api/orders/generate received");
        orderGenerationService.generateRandomOrder();
        return ResponseEntity.noContent().build();
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
        DashboardStatDTO stat = new DashboardStatDTO();
        stat.setLabel(label);
        stat.setValue(value);
        stat.setTrend("");
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
            dto.setDriverName(driver.getName());
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
