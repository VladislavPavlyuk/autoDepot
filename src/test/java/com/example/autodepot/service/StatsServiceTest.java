package com.example.autodepot.service;

import com.example.autodepot.entity.Car;
import com.example.autodepot.entity.Driver;
import com.example.autodepot.entity.Order;
import com.example.autodepot.entity.Trip;
import com.example.autodepot.service.data.DriverService;
import com.example.autodepot.service.data.TripDataService;
import com.example.autodepot.dto.CargoByDestinationDTO;
import com.example.autodepot.dto.DriverEarningsDTO;
import com.example.autodepot.dto.DriverPerformanceDTO;
import com.example.autodepot.dto.StatsSummaryDTO;
import com.example.autodepot.service.stats.CargoByDestinationStatsAggregator;
import com.example.autodepot.service.stats.DriverEarningsStatsAggregator;
import com.example.autodepot.service.stats.DriverPerformanceStatsAggregator;
import com.example.autodepot.service.stats.MostProfitableDriverStatsAggregator;
import com.example.autodepot.service.stats.StatsAggregator;
import com.example.autodepot.service.stats.StatsKeyRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private TripDataService tripDataService;

    @Mock
    private DriverService driverService;

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

        List<StatsAggregator> aggregators = Arrays.asList(
            new DriverPerformanceStatsAggregator(tripDataService),
            new CargoByDestinationStatsAggregator(tripDataService),
            new DriverEarningsStatsAggregator(driverService),
            new MostProfitableDriverStatsAggregator(driverService)
        );
        statsService = new StatsService(new StatsKeyRegistry(aggregators));
    }

    @Test
    void testGetDriverPerformance() {
        // Arrange
        Object[] stat1 = new Object[]{"John Smith", 5L, 5000.0};
        Object[] stat2 = new Object[]{"Michael Johnson", 3L, 6000.0};
        when(tripDataService.findStatsByDriver()).thenReturn(Arrays.asList(stat1, stat2));

        // Act
        List<DriverPerformanceDTO> performance = statsService.getDriverPerformance();

        // Assert
        assertNotNull(performance);
        assertEquals(2, performance.size());
        DriverPerformanceDTO johnStats = performance.stream()
            .filter(dto -> "John Smith".equals(dto.getDriverName()))
            .findFirst()
            .orElseThrow();
        assertEquals(5L, johnStats.getTripCount());
        assertEquals(5000.0, johnStats.getTotalWeight());
    }

    @Test
    void testGetCargoByDestination() {
        // Arrange
        when(tripDataService.findAll()).thenReturn(Arrays.asList(completedTrip1, completedTrip2));

        // Act
        List<CargoByDestinationDTO> cargoByDestination = statsService.getCargoByDestination();

        // Assert
        assertNotNull(cargoByDestination);
        assertEquals(2, cargoByDestination.size());
        CargoByDestinationDTO la = cargoByDestination.stream()
            .filter(dto -> "Los Angeles".equals(dto.getDestination()))
            .findFirst()
            .orElseThrow();
        assertEquals(1L, la.getCargoCount());
    }

    @Test
    void testGetDriverEarnings() {
        // Arrange
        when(driverService.findAll()).thenReturn(Arrays.asList(driver1, driver2));

        // Act
        List<DriverEarningsDTO> earnings = statsService.getDriverEarnings();

        // Assert
        assertNotNull(earnings);
        assertEquals(2, earnings.size());
        DriverEarningsDTO john = earnings.stream()
            .filter(dto -> "John Smith".equals(dto.getDriverName()))
            .findFirst()
            .orElseThrow();
        assertEquals(500.0, john.getEarnings());
    }

    @Test
    void testGetMostProfitable() {
        // Arrange
        when(driverService.findAll()).thenReturn(Arrays.asList(driver1, driver2));

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
        when(driverService.findAll()).thenReturn(List.of());

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
        when(tripDataService.findStatsByDriver()).thenReturn(statsList);
        when(tripDataService.findAll()).thenReturn(Arrays.asList(completedTrip1));
        when(driverService.findAll()).thenReturn(Arrays.asList(driver1));

        // Act
        StatsSummaryDTO allStats = statsService.getAllStats();

        // Assert
        assertNotNull(allStats);
        assertNotNull(allStats.getDriverPerformance());
        assertNotNull(allStats.getCargoByDestination());
        assertNotNull(allStats.getDriverEarnings());
        assertNotNull(allStats.getMostProfitableDriver());
    }
}
