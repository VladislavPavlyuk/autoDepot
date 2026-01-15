package com.example.autodepot.service;

import com.example.autodepot.entity.Car;
import com.example.autodepot.entity.Driver;
import com.example.autodepot.entity.Order;
import com.example.autodepot.entity.Trip;
import com.example.autodepot.repository.CarRepository;
import com.example.autodepot.repository.DriverRepository;
import com.example.autodepot.repository.OrderRepository;
import com.example.autodepot.repository.TripRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private DriverRepository driverRepo;

    @Mock
    private CarRepository carRepo;

    @Mock
    private TripRepository tripRepo;

    @Mock
    private OrderRepository orderRepository;

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
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(driverRepo.findByIsAvailableTrue()).thenReturn(List.of(testDriver));
        when(carRepo.findByIsBrokenFalse()).thenReturn(List.of(testCar));
        when(tripRepo.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(driverRepo.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        assertDoesNotThrow(() -> tripService.createTrip(1L));

        // Assert
        verify(orderRepository, times(1)).findById(1L);
        verify(driverRepo, times(1)).findByIsAvailableTrue();
        verify(carRepo, times(1)).findByIsBrokenFalse();
        verify(tripRepo, times(1)).save(any(Trip.class));
        verify(driverRepo, times(1)).save(any(Driver.class));
    }

    @Test
    void testCreateTrip_OrderNotFound() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> tripService.createTrip(1L));
        assertEquals("Order not found: 1", exception.getMessage());
    }

    @Test
    void testCreateTrip_NoAvailableDrivers() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(driverRepo.findByIsAvailableTrue()).thenReturn(new ArrayList<>());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> tripService.createTrip(1L));
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

        when(orderRepository.findById(2L)).thenReturn(Optional.of(hazardousOrder));
        when(driverRepo.findByIsAvailableTrue()).thenReturn(List.of(juniorDriver, seniorDriver));
        when(carRepo.findByIsBrokenFalse()).thenReturn(List.of(testCar));
        when(tripRepo.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(driverRepo.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        tripService.createTrip(2L);

        // Assert - Senior driver should be selected (experience >= 10)
        verify(tripRepo, times(1)).save(argThat(trip -> 
            trip.getDriver().getExperience() >= 10));
    }

    @Test
    void testCompleteTrip_Success() {
        // Arrange
        Trip trip = new Trip(testOrder, testDriver, testCar);
        trip.setId(1L);
        trip.setStatus(Trip.TripStatus.IN_PROGRESS);

        when(tripRepo.findById(1L)).thenReturn(Optional.of(trip));
        when(tripRepo.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(driverRepo.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(carRepo.save(any(Car.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        tripService.completeTrip(1L, "OK");

        // Assert
        verify(tripRepo, times(1)).findById(1L);
        verify(tripRepo, times(1)).save(argThat(t -> 
            t.getStatus() == Trip.TripStatus.COMPLETED && 
            t.getPayment() != null &&
            t.getCarStatusAfterTrip().equals("OK")));
        verify(driverRepo, times(1)).save(argThat(d -> d.isAvailable()));
        verify(carRepo, times(1)).save(any(Car.class));
    }

    @Test
    void testProcessBreakdown() {
        // Arrange
        Trip trip = new Trip(testOrder, testDriver, testCar);
        trip.setId(1L);
        trip.setStatus(Trip.TripStatus.IN_PROGRESS);

        when(tripRepo.findById(1L)).thenReturn(Optional.of(trip));
        when(tripRepo.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(carRepo.save(any(Car.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        tripService.processBreakdown(1L);

        // Assert
        verify(tripRepo, times(1)).save(argThat(t -> 
            t.getStatus() == Trip.TripStatus.BROKEN));
        verify(carRepo, times(1)).save(argThat(c -> c.isBroken()));
    }

    @Test
    void testRequestRepair() {
        // Arrange
        Trip trip = new Trip(testOrder, testDriver, testCar);
        trip.setId(1L);
        trip.setStatus(Trip.TripStatus.BROKEN);

        when(tripRepo.findById(1L)).thenReturn(Optional.of(trip));
        when(tripRepo.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        tripService.requestRepair(1L);

        // Assert
        verify(tripRepo, times(1)).save(argThat(t -> 
            t.getStatus() == Trip.TripStatus.REPAIR_REQUESTED));
    }
}
