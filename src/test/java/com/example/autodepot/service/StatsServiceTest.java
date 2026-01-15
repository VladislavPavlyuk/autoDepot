package com.example.autodepot.service;

import com.example.autodepot.entity.Car;
import com.example.autodepot.entity.Driver;
import com.example.autodepot.entity.Order;
import com.example.autodepot.entity.Trip;
import com.example.autodepot.repository.DriverRepository;
import com.example.autodepot.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private TripRepository tripRepo;

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private StatsService statsService;

    private Driver driver1;
    private Driver driver2;
    private Trip completedTrip1;
    private Trip completedTrip2;

    @BeforeEach
    void setUp() {
        driver1 = new Driver("John Smith", 5);
        driver1.setId(1L);
        driver1.setEarnings(500.0);

        driver2 = new Driver("Michael Johnson", 8);
        driver2.setId(2L);
        driver2.setEarnings(750.0);

        Order order1 = new Order("New York", "STANDARD", 1000.0);
        Order order2 = new Order("Los Angeles", "FRAGILE", 2000.0);

        completedTrip1 = new Trip(order1, driver1, new Car(2000.0));
        completedTrip1.setId(1L);
        completedTrip1.setStatus(Trip.TripStatus.COMPLETED);
        completedTrip1.setPayment(100.0);

        completedTrip2 = new Trip(order2, driver2, new Car(3000.0));
        completedTrip2.setId(2L);
        completedTrip2.setStatus(Trip.TripStatus.COMPLETED);
        completedTrip2.setPayment(300.0);
    }

    @Test
    void testGetDriverPerformance() {
        // Arrange
        Object[] stat1 = new Object[]{"John Smith", 5L, 5000.0};
        Object[] stat2 = new Object[]{"Michael Johnson", 3L, 6000.0};
        when(tripRepo.findStatsByDriver()).thenReturn(Arrays.asList(stat1, stat2));

        // Act
        Map<String, Object> performance = statsService.getDriverPerformance();

        // Assert
        assertNotNull(performance);
        assertEquals(2, performance.size());
        assertTrue(performance.containsKey("John Smith"));
        assertTrue(performance.containsKey("Michael Johnson"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> driver1Stats = (Map<String, Object>) performance.get("John Smith");
        assertEquals(5L, driver1Stats.get("tripCount"));
        assertEquals(5000.0, driver1Stats.get("totalWeight"));
    }

    @Test
    void testGetCargoByDestination() {
        // Arrange
        when(tripRepo.findAll()).thenReturn(Arrays.asList(completedTrip1, completedTrip2));

        // Act
        Map<String, Long> cargoByDestination = statsService.getCargoByDestination();

        // Assert
        assertNotNull(cargoByDestination);
        assertEquals(2, cargoByDestination.size());
        assertEquals(1L, cargoByDestination.get("New York"));
        assertEquals(1L, cargoByDestination.get("Los Angeles"));
    }

    @Test
    void testGetDriverEarnings() {
        // Arrange
        when(driverRepository.findAll()).thenReturn(Arrays.asList(driver1, driver2));

        // Act
        Map<String, Double> earnings = statsService.getDriverEarnings();

        // Assert
        assertNotNull(earnings);
        assertEquals(2, earnings.size());
        assertEquals(500.0, earnings.get("John Smith"));
        assertEquals(750.0, earnings.get("Michael Johnson"));
    }

    @Test
    void testGetMostProfitable() {
        // Arrange
        when(driverRepository.findAll()).thenReturn(Arrays.asList(driver1, driver2));

        // Act
        String mostProfitable = statsService.getMostProfitable();

        // Assert
        assertNotNull(mostProfitable);
        assertTrue(mostProfitable.contains("Michael Johnson"));
        assertTrue(mostProfitable.contains("750.00"));
    }

    @Test
    void testGetMostProfitable_NoData() {
        // Arrange
        when(driverRepository.findAll()).thenReturn(List.of());

        // Act
        String mostProfitable = statsService.getMostProfitable();

        // Assert
        assertEquals("No data available", mostProfitable);
    }

    @Test
    void testGetAllStats() {
        // Arrange
        Object[] stat = new Object[]{"John Smith", 5L, 5000.0};
        List<Object[]> statsList = new ArrayList<>();
        statsList.add(stat);
        when(tripRepo.findStatsByDriver()).thenReturn(statsList);
        when(tripRepo.findAll()).thenReturn(Arrays.asList(completedTrip1));
        when(driverRepository.findAll()).thenReturn(Arrays.asList(driver1));

        // Act
        Map<String, Object> allStats = statsService.getAllStats();

        // Assert
        assertNotNull(allStats);
        assertTrue(allStats.containsKey("driverPerformance"));
        assertTrue(allStats.containsKey("cargoByDestination"));
        assertTrue(allStats.containsKey("driverEarnings"));
        assertTrue(allStats.containsKey("mostProfitableDriver"));
    }
}
