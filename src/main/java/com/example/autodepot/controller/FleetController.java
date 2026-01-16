package com.example.autodepot.controller;

import com.example.autodepot.dto.OrderDTO;
import com.example.autodepot.entity.Order;
import com.example.autodepot.entity.Trip;
import com.example.autodepot.repository.OrderRepository;
import com.example.autodepot.repository.TripRepository;
import com.example.autodepot.service.OrderGenerationService;
import com.example.autodepot.service.StatsService;
import com.example.autodepot.service.TripService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/fleet")
public class FleetController {
    private final TripService tripService;
    private final StatsService statsService;
    private final OrderRepository orderRepository;
    private final TripRepository tripRepository;
    private final OrderGenerationService orderGenerationService;

    public FleetController(TripService tripService, StatsService statsService, 
                           OrderRepository orderRepository, TripRepository tripRepository,
                           OrderGenerationService orderGenerationService) {
        this.tripService = tripService;
        this.statsService = statsService;
        this.orderRepository = orderRepository;
        this.tripRepository = tripRepository;
        this.orderGenerationService = orderGenerationService;
    }

    @GetMapping("/dashboard")
    public String getDashboard(Model model) {
        Map<String, Object> allStats = statsService.getAllStats();
        model.addAllAttributes(allStats);
        
        List<Order> allOrders = orderRepository.findAll();
        List<Order> pendingOrders = allOrders.stream()
            .filter(o -> !tripRepository.existsByOrderId(o.getId()))
            .toList();
        model.addAttribute("pendingOrders", pendingOrders);
        
        List<Trip> activeTrips = tripRepository.findAll().stream()
            .filter(t -> t.getStatus() == Trip.TripStatus.IN_PROGRESS || 
                        t.getStatus() == Trip.TripStatus.BROKEN ||
                        t.getStatus() == Trip.TripStatus.REPAIR_REQUESTED)
            .toList();
        model.addAttribute("activeTrips", activeTrips);
        
        return "dashboard";
    }

    @PostMapping("/orders")
    public String createOrder(@ModelAttribute OrderDTO orderDTO) {
        Order order = new Order(orderDTO.getDestination(), 
                               orderDTO.getCargoType(), 
                               orderDTO.getWeight());
        orderRepository.save(order);
        return "redirect:/fleet/dashboard";
    }

    @PostMapping("/orders/generate")
    public String generateRandomOrder() {
        orderGenerationService.generateRandomOrder();
        return "redirect:/fleet/dashboard";
    }

    @PostMapping("/trips/assign")
    public String assignTrip(@RequestParam Long orderId, RedirectAttributes redirectAttributes) {
        try {
            tripService.createTrip(orderId);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/fleet/dashboard";
    }

    @PostMapping("/trips/{tripId}/complete")
    public String completeTrip(@PathVariable Long tripId, 
                              @RequestParam String carStatus,
                              RedirectAttributes redirectAttributes) {
        try {
            tripService.completeTrip(tripId, carStatus);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/fleet/dashboard";
    }

    @PostMapping("/trips/{tripId}/breakdown")
    public String reportBreakdown(@PathVariable Long tripId, RedirectAttributes redirectAttributes) {
        try {
            tripService.processBreakdown(tripId);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/fleet/dashboard";
    }

    @PostMapping("/trips/{tripId}/repair")
    public String requestRepair(@PathVariable Long tripId, RedirectAttributes redirectAttributes) {
        try {
            tripService.requestRepair(tripId);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/fleet/dashboard";
    }

    @PostMapping("/trips/simulate-breakdown")
    public String simulateBreakdown() {
        tripService.simulateRandomBreakdown();
        return "redirect:/fleet/dashboard";
    }
}
