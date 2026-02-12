package com.example.autodepot.controller;

import com.example.autodepot.dto.OrderDTO;
import com.example.autodepot.dto.StatsSummaryDTO;
import com.example.autodepot.dto.TripAssignDTO;
import com.example.autodepot.dto.TripCompleteDTO;
import com.example.autodepot.service.FleetDashboardService;
import com.example.autodepot.service.OrderApplicationService;
import com.example.autodepot.service.OrderGenerationService;
import com.example.autodepot.service.StatsService;
import com.example.autodepot.service.TripService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FleetControllerTest {

    private FleetController fleetController;
    private TripService tripService;
    private StatsService statsService;
    private FleetDashboardService fleetDashboardService;
    private OrderApplicationService orderApplicationService;
    private OrderGenerationService orderGenerationService;

    @BeforeEach
    void setUp() {
        tripService = mock(TripService.class);
        statsService = mock(StatsService.class);
        fleetDashboardService = mock(FleetDashboardService.class);
        orderApplicationService = mock(OrderApplicationService.class);
        orderGenerationService = mock(OrderGenerationService.class);

        fleetController = new FleetController(
            tripService, statsService, fleetDashboardService, orderApplicationService, orderGenerationService
        );
    }

    @Test
    void getDashboard_WhenCalled_ReturnsDashboardView() {
        StatsSummaryDTO stats = new StatsSummaryDTO();
        stats.setDriverPerformance(new ArrayList<>());
        stats.setMostProfitableDriver("Ivan Petrenko (€500.00)");
        stats.setDriverEarnings(new ArrayList<>());
        stats.setCargoByDestination(new ArrayList<>());
        when(statsService.getAllStats()).thenReturn(stats);
        when(fleetDashboardService.getPendingOrders()).thenReturn(new ArrayList<>());
        when(fleetDashboardService.getActiveTrips()).thenReturn(new ArrayList<>());

        String actualResult = fleetController.getDashboard(mock(Model.class));
        String expectedResult = "dashboard";

        assertEquals(expectedResult, actualResult);
        verify(statsService, times(1)).getAllStats();
    }

    @Test
    void createOrder_WhenCalled_ReturnsRedirectToDashboard() {
        OrderDTO orderDTO = new OrderDTO("Berlin", "STANDARD", 1000.0);

        String actualResult = fleetController.createOrder(orderDTO);
        String expectedResult = "redirect:/fleet/dashboard";

        assertEquals(expectedResult, actualResult);
        verify(orderApplicationService, times(1)).createOrder(orderDTO);
    }

    @Test
    void generateRandomOrder_WhenCalled_ReturnsRedirectToDashboard() {
        String actualResult = fleetController.generateRandomOrder();
        String expectedResult = "redirect:/fleet/dashboard";

        assertEquals(expectedResult, actualResult);
        verify(orderGenerationService, times(1)).generateRandomOrder();
    }

    @Test
    void assignTrip_WhenOrderIdProvided_ReturnsRedirectToDashboard() {
        TripAssignDTO assignDTO = new TripAssignDTO();
        assignDTO.setOrderId(1L);

        String actualResult = fleetController.assignTrip(assignDTO);
        String expectedResult = "redirect:/fleet/dashboard";

        assertEquals(expectedResult, actualResult);
        verify(tripService, times(1)).createTrip(argThat(dto -> dto.getOrderId().equals(1L)));
    }

    @Test
    void completeTrip_WhenCarStatusProvided_ReturnsRedirectToDashboard() {
        TripCompleteDTO completeDTO = new TripCompleteDTO();
        completeDTO.setCarStatus("OK");

        String actualResult = fleetController.completeTrip(1L, completeDTO);
        String expectedResult = "redirect:/fleet/dashboard";

        assertEquals(expectedResult, actualResult);
        verify(tripService, times(1)).completeTrip(argThat(dto -> "OK".equals(dto.getCarStatus()) && dto.getTripId().equals(1L)));
    }

    @Test
    void reportBreakdown_WhenTripIdProvided_ReturnsRedirectToDashboard() {
        String actualResult = fleetController.reportBreakdown(1L);
        String expectedResult = "redirect:/fleet/dashboard";

        assertEquals(expectedResult, actualResult);
        verify(tripService, times(1)).processBreakdown(1L);
    }

    @Test
    void requestRepair_WhenTripIdProvided_ReturnsRedirectToDashboard() {
        String actualResult = fleetController.requestRepair(1L);
        String expectedResult = "redirect:/fleet/dashboard";

        assertEquals(expectedResult, actualResult);
        verify(tripService, times(1)).requestRepair(1L);
    }

    @Test
    void confirmRepairComplete_WhenTripIdProvided_ReturnsRedirectToDashboard() {
        String actualResult = fleetController.confirmRepairComplete(1L);
        String expectedResult = "redirect:/fleet/dashboard";

        assertEquals(expectedResult, actualResult);
        verify(tripService, times(1)).confirmRepairComplete(1L);
    }

    @Test
    void simulateBreakdown_WhenCalled_ReturnsRedirectToDashboard() {
        String actualResult = fleetController.simulateBreakdown();
        String expectedResult = "redirect:/fleet/dashboard";

        assertEquals(expectedResult, actualResult);
        verify(tripService, times(1)).simulateRandomBreakdown();
    }
}
