package com.example.autodepot.controller;

import com.example.autodepot.dto.OrderDTO;
import com.example.autodepot.entity.Order;
import com.example.autodepot.service.OrderGenerationService;
import com.example.autodepot.service.StatsService;
import com.example.autodepot.service.TripService;
import com.example.autodepot.service.data.OrderService;
import com.example.autodepot.service.data.TripDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FleetControllerTest {

    private FleetController fleetController;
    private TripService tripService;
    private StatsService statsService;
    private OrderService orderService;
    private TripDataService tripDataService;
    private OrderGenerationService orderGenerationService;

    @BeforeEach
    void setUp() {
        tripService = mock(TripService.class);
        statsService = mock(StatsService.class);
        orderService = mock(OrderService.class);
        tripDataService = mock(TripDataService.class);
        orderGenerationService = mock(OrderGenerationService.class);

        fleetController = new FleetController(
            tripService, statsService, orderService, tripDataService, orderGenerationService
        );
    }

    @Test
    void testGetDashboard() {
        // Arrange
        Map<String, Object> stats = new HashMap<>();
        stats.put("driverPerformance", new HashMap<>());
        stats.put("mostProfitableDriver", "John Smith ($500.00)");
        
        when(statsService.getAllStats()).thenReturn(stats);
        when(orderService.findAll()).thenReturn(new ArrayList<>());
        when(tripDataService.existsByOrderId(anyLong())).thenReturn(false);
        when(tripDataService.findAll()).thenReturn(new ArrayList<>());

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
        when(orderService.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        String redirect = fleetController.createOrder(orderDTO);

        // Assert
        assertEquals("redirect:/fleet/dashboard", redirect);
        verify(orderService, times(1)).save(any(Order.class));
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
        String redirect = fleetController.assignTrip(1L, mock(RedirectAttributes.class));

        // Assert
        assertEquals("redirect:/fleet/dashboard", redirect);
        verify(tripService, times(1)).createTrip(1L);
    }

    @Test
    void testCompleteTrip() {
        // Act
        String redirect = fleetController.completeTrip(1L, "OK", mock(RedirectAttributes.class));

        // Assert
        assertEquals("redirect:/fleet/dashboard", redirect);
        verify(tripService, times(1)).completeTrip(1L, "OK");
    }

    @Test
    void testReportBreakdown() {
        // Act
        String redirect = fleetController.reportBreakdown(1L, mock(RedirectAttributes.class));

        // Assert
        assertEquals("redirect:/fleet/dashboard", redirect);
        verify(tripService, times(1)).processBreakdown(1L);
    }

    @Test
    void testRequestRepair() {
        // Act
        String redirect = fleetController.requestRepair(1L, mock(RedirectAttributes.class));

        // Assert
        assertEquals("redirect:/fleet/dashboard", redirect);
        verify(tripService, times(1)).requestRepair(1L);
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
