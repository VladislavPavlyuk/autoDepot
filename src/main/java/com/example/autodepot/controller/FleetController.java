package com.example.autodepot.controller;

import com.example.autodepot.dto.OrderDTO;
import com.example.autodepot.dto.StatsSummaryDTO;
import com.example.autodepot.dto.TripAssignDTO;
import com.example.autodepot.dto.TripBreakdownDTO;
import com.example.autodepot.dto.TripCompleteDTO;
import com.example.autodepot.dto.TripRepairDTO;
import com.example.autodepot.mapper.TripCommandMapper;
import com.example.autodepot.service.FleetDashboardService;
import com.example.autodepot.service.OrderApplicationService;
import com.example.autodepot.service.OrderGenerationService;
import com.example.autodepot.service.StatsService;
import com.example.autodepot.service.TripService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/fleet")
public class FleetController {
    private final TripService tripService;
    private final StatsService statsService;
    private final FleetDashboardService fleetDashboardService;
    private final OrderApplicationService orderApplicationService;
    private final OrderGenerationService orderGenerationService;
    private final TripCommandMapper tripCommandMapper;

    public FleetController(TripService tripService, StatsService statsService, 
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
    public String getDashboard(Model model) {
        StatsSummaryDTO allStats = statsService.getAllStats();
        model.addAttribute("mostProfitableDriver", allStats.getMostProfitableDriver());
        model.addAttribute("driverPerformance", allStats.getDriverPerformance());
        model.addAttribute("driverEarnings", allStats.getDriverEarnings());
        model.addAttribute("cargoByDestination", allStats.getCargoByDestination());
        
        model.addAttribute("pendingOrders", fleetDashboardService.getPendingOrders());
        model.addAttribute("activeTrips", fleetDashboardService.getActiveTrips());
        
        return "dashboard";
    }

    @PostMapping("/orders")
    public String createOrder(@ModelAttribute OrderDTO orderDTO) {
        orderApplicationService.createOrder(orderDTO);
        return "redirect:/fleet/dashboard";
    }

    @PostMapping("/orders/generate")
    public String generateRandomOrder() {
        orderGenerationService.generateRandomOrder();
        return "redirect:/fleet/dashboard";
    }

    @PostMapping("/trips/assign")
    public String assignTrip(@ModelAttribute TripAssignDTO tripAssignDTO) {
        tripService.createTrip(tripAssignDTO);
        return "redirect:/fleet/dashboard";
    }

    @PostMapping("/trips/{tripId}/complete")
    public String completeTrip(@PathVariable Long tripId, @ModelAttribute TripCompleteDTO tripCompleteDTO) {
        tripCompleteDTO.setTripId(tripId);
        tripService.completeTrip(tripCompleteDTO);
        return "redirect:/fleet/dashboard";
    }

    @PostMapping("/trips/{tripId}/breakdown")
    public String reportBreakdown(@PathVariable Long tripId) {
        TripBreakdownDTO breakdownDTO = tripCommandMapper.toBreakdownDto(tripId);
        tripService.processBreakdown(breakdownDTO);
        return "redirect:/fleet/dashboard";
    }

    @PostMapping("/trips/{tripId}/repair")
    public String requestRepair(@PathVariable Long tripId) {
        TripRepairDTO repairDTO = tripCommandMapper.toRepairDto(tripId);
        tripService.requestRepair(repairDTO);
        return "redirect:/fleet/dashboard";
    }

    @PostMapping("/trips/simulate-breakdown")
    public String simulateBreakdown() {
        tripService.simulateRandomBreakdown();
        return "redirect:/fleet/dashboard";
    }
}
