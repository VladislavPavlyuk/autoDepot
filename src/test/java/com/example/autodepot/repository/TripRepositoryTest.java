package com.example.autodepot.repository;

import com.example.autodepot.entity.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripRepositoryTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private OrderRepository orderRepository;

    @Test
    void testSaveAndFindTrip() {
        // Arrange
        Driver driver = new Driver("John Smith", 5);
        driver.setId(1L);

        Car car = new Car(2000.0);
        car.setId(1L);

        Order order = new Order("New York", "STANDARD", 1000.0);
        order.setId(1L);

        Trip trip = new Trip(order, driver, car);
        trip.setId(1L);
        trip.setStatus(Trip.TripStatus.IN_PROGRESS);

        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        // Act
        Trip saved = tripRepository.save(trip);
        Trip found = tripRepository.findById(1L).orElse(null);

        // Assert
        assertNotNull(found);
        assertEquals(trip.getId(), found.getId());
        assertEquals(trip.getOrder().getId(), found.getOrder().getId());
        assertEquals(trip.getDriver().getId(), found.getDriver().getId());
        assertEquals(trip.getCar().getId(), found.getCar().getId());
    }

    @Test
    void testExistsByOrderId() {
        // Arrange
        when(tripRepository.existsByOrderId(1L)).thenReturn(true);
        when(tripRepository.existsByOrderId(999L)).thenReturn(false);

        // Act
        boolean exists = tripRepository.existsByOrderId(1L);
        boolean notExists = tripRepository.existsByOrderId(999L);

        // Assert
        assertTrue(exists);
        assertFalse(notExists);
    }

    @Test
    void testFindStatsByDriver() {
        // Arrange
        Object[] stat1 = new Object[]{"John Smith", 2L, 3000.0};
        Object[] stat2 = new Object[]{"Michael Johnson", 1L, 1000.0};
        when(tripRepository.findStatsByDriver()).thenReturn(List.of(stat1, stat2));

        // Act
        List<Object[]> stats = tripRepository.findStatsByDriver();

        // Assert
        assertNotNull(stats);
        assertEquals(2, stats.size());
        assertEquals("John Smith", stats.get(0)[0]);
    }
}
