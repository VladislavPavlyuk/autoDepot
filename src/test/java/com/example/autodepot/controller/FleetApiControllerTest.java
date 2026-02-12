package com.example.autodepot.controller;

import com.example.autodepot.dto.DashboardResponseDTO;
import com.example.autodepot.dto.OrderDTO;
import com.example.autodepot.exception.BadRequestException;
import com.example.autodepot.service.DashboardApiService;
import com.example.autodepot.service.DriverApplicationService;
import com.example.autodepot.service.OrderApplicationService;
import com.example.autodepot.service.OrderGenerationService;
import com.example.autodepot.service.TripService;
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
    private DashboardApiService dashboardApiService;
    private DriverApplicationService driverApplicationService;
    private OrderApplicationService orderApplicationService;
    private OrderGenerationService orderGenerationService;
    private TripService tripService;

    @BeforeEach
    void setUp() {
        dashboardApiService = mock(DashboardApiService.class);
        driverApplicationService = mock(DriverApplicationService.class);
        orderApplicationService = mock(OrderApplicationService.class);
        orderGenerationService = mock(OrderGenerationService.class);
        tripService = mock(TripService.class);
        controller = new FleetApiController(
            tripService, dashboardApiService, orderApplicationService,
            orderGenerationService, driverApplicationService
        );
    }

    @Test
    void getDashboard_WhenCalled_ReturnsDtoWithAllSections() {
        DashboardResponseDTO dto = new DashboardResponseDTO();
        dto.setStats(List.of());
        dto.setOrders(List.of());
        dto.setTrips(List.of());
        dto.setActivity(List.of());
        dto.setDriverPerformance(List.of());
        when(dashboardApiService.getDashboardResponse()).thenReturn(dto);

        DashboardResponseDTO result = controller.getDashboard();

        assertNotNull(result);
        assertNotNull(result.getStats());
        assertNotNull(result.getOrders());
        assertNotNull(result.getTrips());
        assertNotNull(result.getActivity());
        assertNotNull(result.getDriverPerformance());
    }

    @Test
    void createDriver_WhenBodyEmpty_ThrowsBadRequest() {
        when(driverApplicationService.parseDriverPayload(null))
            .thenThrow(new BadRequestException("Request body is required. Send JSON: name, licenseYear, licenseCategories (array)."));
        BadRequestException ex = assertThrows(BadRequestException.class, () -> controller.createDriver(null));
        assertTrue(ex.getMessage().contains("Request body"));
    }

    @Test
    void createDriver_WhenBodyEmptyBytes_ThrowsBadRequest() {
        when(driverApplicationService.parseDriverPayload(any(byte[].class)))
            .thenThrow(new BadRequestException("Request body is required. Send JSON: name, licenseYear, licenseCategories (array)."));
        BadRequestException ex = assertThrows(BadRequestException.class,
            () -> controller.createDriver(new byte[0]));
        assertTrue(ex.getMessage().contains("Request body"));
    }

    @Test
    void createDriver_WhenInvalidJson_ThrowsBadRequest() {
        when(driverApplicationService.parseDriverPayload(any(byte[].class)))
            .thenThrow(new BadRequestException("Invalid JSON: parse error"));
        BadRequestException ex = assertThrows(BadRequestException.class,
            () -> controller.createDriver("{ invalid }".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        assertTrue(ex.getMessage().contains("Invalid JSON"));
    }

    @Test
    void createDriver_WhenNameMissing_ThrowsBadRequest() {
        String body = "{\"licenseYear\": 2015, \"licenseCategories\": [\"B\"]}";
        var dto = new com.example.autodepot.dto.DriverCreateDTO();
        dto.setName(null);
        dto.setLicenseYear(2015);
        dto.setLicenseCategories(List.of("B"));
        when(driverApplicationService.parseDriverPayload(any(byte[].class))).thenReturn(dto);
        doThrow(new BadRequestException("Driver name is required")).when(driverApplicationService).createDriver(any());
        BadRequestException ex = assertThrows(BadRequestException.class,
            () -> controller.createDriver(body.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        assertTrue(ex.getMessage().toLowerCase().contains("name"));
    }

    @Test
    void createDriver_WhenCategoriesInvalid_ThrowsBadRequest() {
        String body = "{\"name\": \"Ivan\", \"licenseYear\": 2015, \"licenseCategories\": [\"X\"]}";
        var dto = new com.example.autodepot.dto.DriverCreateDTO();
        dto.setName("Ivan");
        dto.setLicenseYear(2015);
        dto.setLicenseCategories(List.of("X"));
        when(driverApplicationService.parseDriverPayload(any(byte[].class))).thenReturn(dto);
        doThrow(new BadRequestException("Driver license categories must be one or more of A, B, C, D, E"))
            .when(driverApplicationService).createDriver(any());
        BadRequestException ex = assertThrows(BadRequestException.class,
            () -> controller.createDriver(body.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        assertTrue(ex.getMessage().contains("A, B, C, D, E"));
    }

    @Test
    void createDriver_WhenValid_Returns201() {
        String body = "{\"name\": \"Ivan Petrenko\", \"licenseYear\": 2015, \"licenseCategories\": [\"B\", \"C\"]}";
        var dto = new com.example.autodepot.dto.DriverCreateDTO();
        dto.setName("Ivan Petrenko");
        dto.setLicenseYear(2015);
        dto.setLicenseCategories(List.of("B", "C"));
        when(driverApplicationService.parseDriverPayload(any(byte[].class))).thenReturn(dto);
        doNothing().when(driverApplicationService).createDriver(any());

        ResponseEntity<java.util.Map<String, String>> res = controller.createDriver(body.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        assertEquals(HttpStatus.CREATED, res.getStatusCode());
    }

    @Test
    void generateRandomOrder_WhenSuccess_Returns204() {
        doNothing().when(orderGenerationService).generateRandomOrder();
        ResponseEntity<Void> res = controller.generateRandomOrder();
        assertEquals(HttpStatus.NO_CONTENT, res.getStatusCode());
    }

    @Test
    void createOrder_WhenDestinationEmpty_ThrowsBadRequest() {
        OrderDTO dto = new OrderDTO("", "STANDARD", 1000.0);
        doThrow(new BadRequestException("Destination is required")).when(orderApplicationService).createOrder(any());
        BadRequestException ex = assertThrows(BadRequestException.class, () -> controller.createOrder(dto));
        assertTrue(ex.getMessage().contains("Destination"));
    }

    @Test
    void createOrder_WhenWeightInvalid_ThrowsBadRequest() {
        OrderDTO dto = new OrderDTO("Berlin", "STANDARD", -1.0);
        doThrow(new BadRequestException("Weight must be between 0 and 100000 kg")).when(orderApplicationService).createOrder(any());
        BadRequestException ex = assertThrows(BadRequestException.class, () -> controller.createOrder(dto));
        assertTrue(ex.getMessage().contains("Weight"));
    }

    @Test
    void createOrder_WhenValid_Returns204() {
        OrderDTO dto = new OrderDTO("Berlin", "STANDARD", 1000.0);
        doNothing().when(orderApplicationService).createOrder(any());
        ResponseEntity<Void> res = controller.createOrder(dto);
        assertEquals(HttpStatus.NO_CONTENT, res.getStatusCode());
    }

    @Test
    void generateRandomOrder_WhenServiceThrows_PropagatesException() {
        doThrow(new RuntimeException("DB error")).when(orderGenerationService).generateRandomOrder();
        RuntimeException ex = assertThrows(RuntimeException.class, () -> controller.generateRandomOrder());
        assertTrue(ex.getMessage().contains("DB error"));
    }
}
