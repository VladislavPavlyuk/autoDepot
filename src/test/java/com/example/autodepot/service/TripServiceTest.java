package com.example.autodepot.service;

import com.example.autodepot.dto.TripAssignDTO;
import com.example.autodepot.dto.TripBreakdownDTO;
import com.example.autodepot.dto.TripCompleteDTO;
import com.example.autodepot.dto.TripRepairDTO;
import com.example.autodepot.entity.Car;
import com.example.autodepot.entity.Driver;
import com.example.autodepot.entity.Order;
import com.example.autodepot.entity.Trip;
import com.example.autodepot.mapper.TripCommandMapper;
import com.example.autodepot.service.command.TripAssignCommand;
import com.example.autodepot.service.command.TripBreakdownCommand;
import com.example.autodepot.service.command.TripCompleteCommand;
import com.example.autodepot.service.command.TripRepairCommand;
import com.example.autodepot.service.data.CarService;
import com.example.autodepot.service.data.DriverService;
import com.example.autodepot.service.data.OrderService;
import com.example.autodepot.service.data.TripDataService;
import com.example.autodepot.service.logging.TripEventLogger;
import com.example.autodepot.service.payment.PaymentCalculator;
import com.example.autodepot.service.selection.CarSelectionPolicy;
import com.example.autodepot.service.selection.DriverSelectionPolicy;
import com.example.autodepot.service.simulation.BreakdownSimulator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private DriverService driverService;

    @Mock
    private CarService carService;

    @Mock
    private TripDataService tripDataService;

    @Mock
    private OrderService orderService;

    @Mock
    private DriverSelectionPolicy driverSelectionPolicy;

    @Mock
    private CarSelectionPolicy carSelectionPolicy;

    @Mock
    private PaymentCalculator paymentCalculator;

    @Mock
    private TripEventLogger tripEventLogger;

    @Mock
    private BreakdownSimulator breakdownSimulator;

    @Mock
    private TripCommandMapper tripCommandMapper;

    @InjectMocks
    private TripService tripService;

    private Order testOrder;
    private Driver testDriver;
    private Car testCar;

    @BeforeEach
    void setUp() {
        testOrder = new Order("New York", "STANDARD", 1000.0);
        testOrder.setId(1L);

        testDriver = new Driver("John Smith", 5);
        testDriver.setId(1L);
        testDriver.setAvailable(true);

        testCar = new Car(2000.0);
        testCar.setId(1L);
        testCar.setBroken(false);
    }

    @Test
    void testCreateTrip_Success() {
        // Arrange
        when(orderService.findById(1L)).thenReturn(Optional.of(testOrder));
        when(driverService.findAvailableDrivers()).thenReturn(List.of(testDriver));
        when(carService.findAvailableCars()).thenReturn(List.of(testCar));
        when(driverSelectionPolicy.selectDriver(eq(testOrder), anyList()))
            .thenReturn(Optional.of(testDriver));
        when(carSelectionPolicy.selectCar(eq(testOrder), anyList()))
            .thenReturn(Optional.of(testCar));
        when(tripDataService.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(driverService.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TripAssignDTO assignDTO = new TripAssignDTO();
        assignDTO.setOrderId(1L);
        TripAssignCommand assignCommand = new TripAssignCommand();
        assignCommand.setOrderId(1L);
        when(tripCommandMapper.toCommand(assignDTO)).thenReturn(assignCommand);
        assertDoesNotThrow(() -> tripService.createTrip(assignDTO));

        // Assert
        verify(orderService, times(1)).findById(1L);
        verify(driverService, times(1)).findAvailableDrivers();
        verify(carService, times(1)).findAvailableCars();
        verify(tripDataService, times(1)).save(any(Trip.class));
        verify(driverService, times(1)).save(any(Driver.class));
    }

    @Test
    void testCreateTrip_OrderNotFound() {
        // Arrange
        when(orderService.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        TripAssignDTO assignDTO = new TripAssignDTO();
        assignDTO.setOrderId(1L);
        TripAssignCommand assignCommand = new TripAssignCommand();
        assignCommand.setOrderId(1L);
        when(tripCommandMapper.toCommand(assignDTO)).thenReturn(assignCommand);
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> tripService.createTrip(assignDTO));
        assertEquals("Order not found: 1", exception.getMessage());
    }

    @Test
    void testCreateTrip_NoAvailableDrivers() {
        // Arrange
        when(orderService.findById(1L)).thenReturn(Optional.of(testOrder));
        when(driverService.findAvailableDrivers()).thenReturn(new ArrayList<>());
        when(driverSelectionPolicy.selectDriver(eq(testOrder), anyList()))
            .thenReturn(Optional.empty());

        // Act & Assert
        TripAssignDTO assignDTO = new TripAssignDTO();
        assignDTO.setOrderId(1L);
        TripAssignCommand assignCommand = new TripAssignCommand();
        assignCommand.setOrderId(1L);
        when(tripCommandMapper.toCommand(assignDTO)).thenReturn(assignCommand);
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> tripService.createTrip(assignDTO));
        assertTrue(exception.getMessage().contains("No available drivers"));
    }

    @Test
    void testCreateTrip_DriverExperienceRequirement() {
        // Arrange
        Order hazardousOrder = new Order("Los Angeles", "HAZARDOUS", 1000.0);
        hazardousOrder.setId(2L);
        
        Driver juniorDriver = new Driver("Junior Driver", 3);
        juniorDriver.setId(2L);
        juniorDriver.setAvailable(true);
        
        Driver seniorDriver = new Driver("Senior Driver", 12);
        seniorDriver.setId(3L);
        seniorDriver.setAvailable(true);

        when(orderService.findById(2L)).thenReturn(Optional.of(hazardousOrder));
        when(driverService.findAvailableDrivers()).thenReturn(List.of(juniorDriver, seniorDriver));
        when(carService.findAvailableCars()).thenReturn(List.of(testCar));
        when(driverSelectionPolicy.selectDriver(eq(hazardousOrder), anyList()))
            .thenReturn(Optional.of(seniorDriver));
        when(carSelectionPolicy.selectCar(eq(hazardousOrder), anyList()))
            .thenReturn(Optional.of(testCar));
        when(tripDataService.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(driverService.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TripAssignDTO assignDTO = new TripAssignDTO();
        assignDTO.setOrderId(2L);
        TripAssignCommand assignCommand = new TripAssignCommand();
        assignCommand.setOrderId(2L);
        when(tripCommandMapper.toCommand(assignDTO)).thenReturn(assignCommand);
        tripService.createTrip(assignDTO);

        // Assert - Senior driver should be selected (experience >= 10)
        verify(tripDataService, times(1)).save(argThat(trip -> 
            trip.getDriver().getExperience() >= 10));
    }

    @Test
    void testCompleteTrip_Success() {
        // Arrange
        Trip trip = new Trip(testOrder, testDriver, testCar);
        trip.setId(1L);
        trip.setStatus(Trip.TripStatus.IN_PROGRESS);

        when(tripDataService.findById(1L)).thenReturn(Optional.of(trip));
        when(paymentCalculator.calculatePayment(eq(testOrder))).thenReturn(123.0);
        when(tripDataService.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(driverService.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(carService.save(any(Car.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TripCompleteDTO completeDTO = new TripCompleteDTO();
        completeDTO.setTripId(1L);
        completeDTO.setCarStatus("OK");
        TripCompleteCommand completeCommand = new TripCompleteCommand();
        completeCommand.setTripId(1L);
        completeCommand.setCarStatus("OK");
        when(tripCommandMapper.toCommand(completeDTO)).thenReturn(completeCommand);
        tripService.completeTrip(completeDTO);

        // Assert
        verify(tripDataService, times(1)).findById(1L);
        verify(tripDataService, times(1)).save(argThat(t -> 
            t.getStatus() == Trip.TripStatus.COMPLETED && 
            t.getPayment() != null &&
            t.getCarStatusAfterTrip().equals("OK")));
        verify(driverService, times(1)).save(argThat(d -> d.isAvailable()));
        verify(carService, times(1)).save(any(Car.class));
    }

    @Test
    void testProcessBreakdown() {
        // Arrange
        Trip trip = new Trip(testOrder, testDriver, testCar);
        trip.setId(1L);
        trip.setStatus(Trip.TripStatus.IN_PROGRESS);

        when(tripDataService.findById(1L)).thenReturn(Optional.of(trip));
        when(tripDataService.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(carService.save(any(Car.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TripBreakdownDTO breakdownDTO = new TripBreakdownDTO();
        breakdownDTO.setTripId(1L);
        TripBreakdownCommand breakdownCommand = new TripBreakdownCommand();
        breakdownCommand.setTripId(1L);
        when(tripCommandMapper.toCommand(breakdownDTO)).thenReturn(breakdownCommand);
        tripService.processBreakdown(breakdownDTO);

        // Assert
        verify(tripDataService, times(1)).save(argThat(t -> 
            t.getStatus() == Trip.TripStatus.BROKEN));
        verify(carService, times(1)).save(argThat(c -> c.isBroken()));
    }

    @Test
    void testRequestRepair() {
        // Arrange
        Trip trip = new Trip(testOrder, testDriver, testCar);
        trip.setId(1L);
        trip.setStatus(Trip.TripStatus.BROKEN);

        when(tripDataService.findById(1L)).thenReturn(Optional.of(trip));
        when(tripDataService.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TripRepairDTO repairDTO = new TripRepairDTO();
        repairDTO.setTripId(1L);
        TripRepairCommand repairCommand = new TripRepairCommand();
        repairCommand.setTripId(1L);
        when(tripCommandMapper.toCommand(repairDTO)).thenReturn(repairCommand);
        tripService.requestRepair(repairDTO);

        // Assert
        verify(tripDataService, times(1)).save(argThat(t -> 
            t.getStatus() == Trip.TripStatus.REPAIR_REQUESTED));
    }
}
