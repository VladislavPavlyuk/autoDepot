package com.example.autodepot.controller;

import com.example.autodepot.dto.DashboardResponseDTO;
import com.example.autodepot.dto.OrderDTO;
import com.example.autodepot.dto.StatsSummaryDTO;
import com.example.autodepot.entity.Driver;
import com.example.autodepot.exception.BadRequestException;
import com.example.autodepot.service.FleetDashboardService;
import com.example.autodepot.service.OrderApplicationService;
import com.example.autodepot.service.OrderGenerationService;
import com.example.autodepot.service.StatsService;
import com.example.autodepot.service.TripService;
import com.example.autodepot.service.data.DriverService;
import com.example.autodepot.mapper.TripCommandMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FleetApiControllerTest {

    private FleetApiController controller;
    private StatsService statsService;
    private FleetDashboardService fleetDashboardService;
    private DriverService driverService;
    private OrderApplicationService orderApplicationService;
    private OrderGenerationService orderGenerationService;
    private TripService tripService;
    private TripCommandMapper tripCommandMapper;

    @BeforeEach
    void setUp() {
        statsService = mock(StatsService.class);
        fleetDashboardService = mock(FleetDashboardService.class);
        driverService = mock(DriverService.class);
        orderApplicationService = mock(OrderApplicationService.class);
        orderGenerationService = mock(OrderGenerationService.class);
        tripService = mock(TripService.class);
        tripCommandMapper = mock(TripCommandMapper.class);
        controller = new FleetApiController(
            tripService, statsService, fleetDashboardService, orderApplicationService,
            orderGenerationService, tripCommandMapper, driverService
        );
    }

    @Test
    void getDashboard_WhenCalled_ReturnsDtoWithAllSections() {
        StatsSummaryDTO stats = new StatsSummaryDTO();
        stats.setMostProfitableDriver("Ivan Petrenko (€500.00)");
        stats.setDriverPerformance(List.of());
        stats.setDriverEarnings(List.of());
        stats.setCargoByDestination(List.of());
        when(statsService.getAllStats()).thenReturn(stats);
        when(fleetDashboardService.getPendingOrders()).thenReturn(List.of());
        when(fleetDashboardService.getActiveTrips()).thenReturn(List.of());
        when(driverService.findAll()).thenReturn(List.of());

        DashboardResponseDTO result = controller.getDashboard();

        boolean actualResult = result != null && result.getStats() != null && result.getOrders() != null
            && result.getTrips() != null && result.getActivity() != null && result.getDriverPerformance() != null;
        boolean expectedResult = true;
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void createDriver_WhenBodyEmpty_ThrowsBadRequest() {
        BadRequestException ex = assertThrows(BadRequestException.class, () -> controller.createDriver(null));
        boolean actualResult = ex.getMessage().contains("Request body");
        boolean expectedResult = true;
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void createDriver_WhenBodyEmptyBytes_ThrowsBadRequest() {
        BadRequestException ex = assertThrows(BadRequestException.class,
            () -> controller.createDriver(new byte[0]));
        boolean actualResult = ex.getMessage().contains("Request body");
        boolean expectedResult = true;
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void createDriver_WhenInvalidJson_ThrowsBadRequest() {
        BadRequestException ex = assertThrows(BadRequestException.class,
            () -> controller.createDriver("{ invalid }".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        boolean actualResult = ex.getMessage().contains("Invalid JSON");
        boolean expectedResult = true;
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void createDriver_WhenNameMissing_ThrowsBadRequest() {
        String body = "{\"licenseYear\": 2015, \"licenseCategories\": [\"B\"]}";
        BadRequestException ex = assertThrows(BadRequestException.class,
            () -> controller.createDriver(body.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        boolean actualResult = ex.getMessage().toLowerCase().contains("name");
        boolean expectedResult = true;
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void createDriver_WhenCategoriesInvalid_ThrowsBadRequest() {
        String body = "{\"name\": \"Ivan\", \"licenseYear\": 2015, \"licenseCategories\": [\"X\"]}";
        BadRequestException ex = assertThrows(BadRequestException.class,
            () -> controller.createDriver(body.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        boolean actualResult = ex.getMessage().contains("A, B, C, D, E");
        boolean expectedResult = true;
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void createDriver_WhenValid_Returns201() {
        String body = "{\"name\": \"Ivan Petrenko\", \"licenseYear\": 2015, \"licenseCategories\": [\"B\", \"C\"]}";
        ResponseEntity<java.util.Map<String, String>> res = controller.createDriver(body.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        assertEquals(HttpStatus.CREATED, res.getStatusCode());
    }

    @Test
    void generateRandomOrder_WhenSuccess_Returns204() {
        doNothing().when(orderGenerationService).generateRandomOrder();
        ResponseEntity<?> res = controller.generateRandomOrder();
        assertEquals(HttpStatus.NO_CONTENT, res.getStatusCode());
    }

    @Test
    void createOrder_WhenDestinationEmpty_ThrowsBadRequest() {
        OrderDTO dto = new OrderDTO("", "STANDARD", 1000.0);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> controller.createOrder(dto));
        boolean actualResult = ex.getMessage().contains("Destination");
        boolean expectedResult = true;
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void createOrder_WhenWeightInvalid_ThrowsBadRequest() {
        OrderDTO dto = new OrderDTO("Berlin", "STANDARD", -1.0);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> controller.createOrder(dto));
        boolean actualResult = ex.getMessage().contains("Weight");
        boolean expectedResult = true;
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void createOrder_WhenValid_Returns204() {
        OrderDTO dto = new OrderDTO("Berlin", "STANDARD", 1000.0);
        ResponseEntity<?> res = controller.createOrder(dto);
        assertEquals(HttpStatus.NO_CONTENT, res.getStatusCode());
    }

    @Test
    void generateRandomOrder_WhenServiceThrows_PropagatesException() {
        doThrow(new RuntimeException("DB error")).when(orderGenerationService).generateRandomOrder();
        RuntimeException ex = assertThrows(RuntimeException.class, () -> controller.generateRandomOrder());
        boolean actualResult = ex.getMessage().contains("DB error");
        boolean expectedResult = true;
        assertEquals(expectedResult, actualResult);
    }
}
