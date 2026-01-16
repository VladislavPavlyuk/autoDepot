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
    void testGetDashboard() {
        // Arrange
        StatsSummaryDTO stats = new StatsSummaryDTO();
        stats.setDriverPerformance(new ArrayList<>());
        stats.setMostProfitableDriver("John Smith ($500.00)");
        stats.setDriverEarnings(new ArrayList<>());
        stats.setCargoByDestination(new ArrayList<>());
        when(statsService.getAllStats()).thenReturn(stats);
        when(fleetDashboardService.getPendingOrders()).thenReturn(new ArrayList<>());
        when(fleetDashboardService.getActiveTrips()).thenReturn(new ArrayList<>());

        // Act
        String viewName = fleetController.getDashboard(mock(Model.class));

        // Assert
        assertEquals("dashboard", viewName);
        verify(statsService, times(1)).getAllStats();
    }

    @Test
    void testCreateOrder() {
        // Arrange
        OrderDTO orderDTO = new OrderDTO("New York", "STANDARD", 1000.0);
        // Act
        String redirect = fleetController.createOrder(orderDTO);

        // Assert
        assertEquals("redirect:/fleet/dashboard", redirect);
        verify(orderApplicationService, times(1)).createOrder(orderDTO);
    }

    @Test
    void testGenerateRandomOrder() {
        // Act
        String redirect = fleetController.generateRandomOrder();

        // Assert
        assertEquals("redirect:/fleet/dashboard", redirect);
        verify(orderGenerationService, times(1)).generateRandomOrder();
    }

    @Test
    void testAssignTrip() {
        // Act
        TripAssignDTO assignDTO = new TripAssignDTO();
        assignDTO.setOrderId(1L);
        String redirect = fleetController.assignTrip(assignDTO, mock(RedirectAttributes.class));

        // Assert
        assertEquals("redirect:/fleet/dashboard", redirect);
        verify(tripService, times(1)).createTrip(argThat(dto -> dto.getOrderId().equals(1L)));
    }

    @Test
    void testCompleteTrip() {
        // Act
        TripCompleteDTO completeDTO = new TripCompleteDTO();
        completeDTO.setCarStatus("OK");
        String redirect = fleetController.completeTrip(1L, completeDTO, mock(RedirectAttributes.class));

        // Assert
        assertEquals("redirect:/fleet/dashboard", redirect);
        verify(tripService, times(1)).completeTrip(argThat(dto -> "OK".equals(dto.getCarStatus()) && dto.getTripId().equals(1L)));
    }

    @Test
    void testReportBreakdown() {
        // Act
        TripBreakdownDTO breakdownDTO = new TripBreakdownDTO();
        breakdownDTO.setTripId(1L);
        when(tripCommandMapper.toBreakdownDto(1L)).thenReturn(breakdownDTO);
        String redirect = fleetController.reportBreakdown(1L, mock(RedirectAttributes.class));

        // Assert
        assertEquals("redirect:/fleet/dashboard", redirect);
        verify(tripService, times(1)).processBreakdown(argThat(dto -> dto.getTripId().equals(1L)));
    }

    @Test
    void testRequestRepair() {
        // Act
        TripRepairDTO repairDTO = new TripRepairDTO();
        repairDTO.setTripId(1L);
        when(tripCommandMapper.toRepairDto(1L)).thenReturn(repairDTO);
        String redirect = fleetController.requestRepair(1L, mock(RedirectAttributes.class));

        // Assert
        assertEquals("redirect:/fleet/dashboard", redirect);
        verify(tripService, times(1)).requestRepair(argThat(dto -> dto.getTripId().equals(1L)));
    }

    @Test
    void testSimulateBreakdown() {
        // Act
        String redirect = fleetController.simulateBreakdown();

        // Assert
        assertEquals("redirect:/fleet/dashboard", redirect);
        verify(tripService, times(1)).simulateRandomBreakdown();
    }
}
