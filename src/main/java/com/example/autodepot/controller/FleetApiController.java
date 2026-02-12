package com.example.autodepot.controller;

import com.example.autodepot.dto.DashboardResponseDTO;
import com.example.autodepot.dto.DriverCreateDTO;
import com.example.autodepot.dto.OrderDTO;
import com.example.autodepot.dto.TripAssignDTO;
import com.example.autodepot.dto.TripBreakdownDTO;
import com.example.autodepot.dto.TripCompleteDTO;
import com.example.autodepot.dto.TripRepairDTO;
import com.example.autodepot.mapper.TripCommandMapper;
import com.example.autodepot.service.DashboardApiService;
import com.example.autodepot.service.DriverApplicationService;
import com.example.autodepot.service.OrderApplicationService;
import com.example.autodepot.service.OrderGenerationService;
import com.example.autodepot.service.TripService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class FleetApiController {
    private static final Logger log = LoggerFactory.getLogger(FleetApiController.class);

    private final TripService tripService;
    private final DashboardApiService dashboardApiService;
    private final OrderApplicationService orderApplicationService;
    private final OrderGenerationService orderGenerationService;
    private final TripCommandMapper tripCommandMapper;
    private final DriverApplicationService driverApplicationService;

    public FleetApiController(TripService tripService,
                              DashboardApiService dashboardApiService,
                              OrderApplicationService orderApplicationService,
                              OrderGenerationService orderGenerationService,
                              TripCommandMapper tripCommandMapper,
                              DriverApplicationService driverApplicationService) {
        this.tripService = tripService;
        this.dashboardApiService = dashboardApiService;
        this.orderApplicationService = orderApplicationService;
        this.orderGenerationService = orderGenerationService;
        this.tripCommandMapper = tripCommandMapper;
        this.driverApplicationService = driverApplicationService;
    }

    @GetMapping("/dashboard")
    public DashboardResponseDTO getDashboard() {
        return dashboardApiService.getDashboardResponse();
    }

    @PostMapping(value = "/drivers", consumes = {"application/json", "application/json;charset=UTF-8"})
    public ResponseEntity<Map<String, String>> createDriver(@RequestBody(required = false) byte[] rawBytes) {
        var dto = driverApplicationService.parseDriverPayload(rawBytes);
        driverApplicationService.createDriver(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "created"));
    }

    @PatchMapping("/drivers/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateDriver(@PathVariable Long id, @RequestBody DriverCreateDTO dto) {
        driverApplicationService.updateDriver(id, dto);
    }

    @PostMapping("/orders")
    public ResponseEntity<Void> createOrder(@RequestBody OrderDTO orderDTO) {
        orderApplicationService.createOrder(orderDTO);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/orders/generate")
    public ResponseEntity<Void> generateRandomOrder() {
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
}
