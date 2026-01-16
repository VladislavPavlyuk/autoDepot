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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    private TripCommandMapper tripCommandMapper;

    @BeforeEach
    void setUp() {
        tripService = mock(TripService.class);
        statsService = mock(StatsService.class);
        fleetDashboardService = mock(FleetDashboardService.class);
        orderApplicationService = mock(OrderApplicationService.class);
        orderGenerationService = mock(OrderGenerationService.class);
        tripCommandMapper = mock(TripCommandMapper.class);

        fleetController = new FleetController(
            tripService, statsService, fleetDashboardService, orderApplicationService, orderGenerationService, tripCommandMapper
        );
    }

    @Test
    void getDashboard_WhenCalled_ReturnsDashboardView() {
        StatsSummaryDTO stats = new StatsSummaryDTO();
        stats.setDriverPerformance(new ArrayList<>());
        stats.setMostProfitableDriver("John Smith ($500.00)");
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
        OrderDTO orderDTO = new OrderDTO("New York", "STANDARD", 1000.0);

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

        String actualResult = fleetController.assignTrip(assignDTO, mock(RedirectAttributes.class));
        String expectedResult = "redirect:/fleet/dashboard";

        assertEquals(expectedResult, actualResult);
        verify(tripService, times(1)).createTrip(argThat(dto -> dto.getOrderId().equals(1L)));
    }

    @Test
    void completeTrip_WhenCarStatusProvided_ReturnsRedirectToDashboard() {
        TripCompleteDTO completeDTO = new TripCompleteDTO();
        completeDTO.setCarStatus("OK");

        String actualResult = fleetController.completeTrip(1L, completeDTO, mock(RedirectAttributes.class));
        String expectedResult = "redirect:/fleet/dashboard";

        assertEquals(expectedResult, actualResult);
        verify(tripService, times(1)).completeTrip(argThat(dto -> "OK".equals(dto.getCarStatus()) && dto.getTripId().equals(1L)));
    }

    @Test
    void reportBreakdown_WhenTripIdProvided_ReturnsRedirectToDashboard() {
        TripBreakdownDTO breakdownDTO = new TripBreakdownDTO();
        breakdownDTO.setTripId(1L);
        when(tripCommandMapper.toBreakdownDto(1L)).thenReturn(breakdownDTO);

        String actualResult = fleetController.reportBreakdown(1L, mock(RedirectAttributes.class));
        String expectedResult = "redirect:/fleet/dashboard";

        assertEquals(expectedResult, actualResult);
        verify(tripService, times(1)).processBreakdown(argThat(dto -> dto.getTripId().equals(1L)));
    }

    @Test
    void requestRepair_WhenTripIdProvided_ReturnsRedirectToDashboard() {
        TripRepairDTO repairDTO = new TripRepairDTO();
        repairDTO.setTripId(1L);
        when(tripCommandMapper.toRepairDto(1L)).thenReturn(repairDTO);

        String actualResult = fleetController.requestRepair(1L, mock(RedirectAttributes.class));
        String expectedResult = "redirect:/fleet/dashboard";

        assertEquals(expectedResult, actualResult);
        verify(tripService, times(1)).requestRepair(argThat(dto -> dto.getTripId().equals(1L)));
    }

    @Test
    void simulateBreakdown_WhenCalled_ReturnsRedirectToDashboard() {
        String actualResult = fleetController.simulateBreakdown();
        String expectedResult = "redirect:/fleet/dashboard";

        assertEquals(expectedResult, actualResult);
        verify(tripService, times(1)).simulateRandomBreakdown();
    }
}
