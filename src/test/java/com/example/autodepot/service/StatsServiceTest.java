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
import com.example.autodepot.mapper.StatsMapper;
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
        driver1 = new Driver("Ivan Petrenko", 5);
        driver1.setId(1L);
        driver1.setEarnings(500.0);

        driver2 = new Driver("Mykhailo Kovalenko", 8);
        driver2.setId(2L);
        driver2.setEarnings(750.0);

        Order order1 = new Order("Berlin", "STANDARD", 1000.0);
        Order order2 = new Order("Paris", "FRAGILE", 2000.0);

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
        StatsMapper statsMapper = new StatsMapper() {
        };
        statsService = new StatsService(new StatsKeyRegistry(aggregators), statsMapper);
    }

    @Test
    void getDriverPerformance_WhenStatsAvailable_ReturnsExpectedDriverStats() {
        Object[] stat1 = new Object[]{"Ivan Petrenko", 5L, 5000.0};
        Object[] stat2 = new Object[]{"Mykhailo Kovalenko", 3L, 6000.0};
        when(tripDataService.findStatsByDriver()).thenReturn(Arrays.asList(stat1, stat2));

        List<DriverPerformanceDTO> performance = statsService.getDriverPerformance();

        DriverPerformanceDTO johnStats = performance.stream()
            .filter(dto -> "Ivan Petrenko".equals(dto.getDriverName()))
            .findFirst()
            .orElse(null);
        boolean actualResult = performance.size() == 2
            && johnStats != null
            && johnStats.getTripCount() == 5L
            && johnStats.getTotalWeight() == 5000.0;
        boolean expectedResult = true;
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void getCargoByDestination_WhenCompletedTripsExist_ReturnsExpectedCounts() {
        when(tripDataService.findAll()).thenReturn(Arrays.asList(completedTrip1, completedTrip2));

        List<CargoByDestinationDTO> cargoByDestination = statsService.getCargoByDestination();

        CargoByDestinationDTO la = cargoByDestination.stream()
            .filter(dto -> "Paris".equals(dto.getDestination()))
            .findFirst()
            .orElse(null);
        boolean actualResult = cargoByDestination.size() == 2
            && la != null
            && la.getCargoCount() == 1L;
        boolean expectedResult = true;
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void getDriverEarnings_WhenDriversExist_ReturnsExpectedEarnings() {
        when(driverService.findAll()).thenReturn(Arrays.asList(driver1, driver2));

        List<DriverEarningsDTO> earnings = statsService.getDriverEarnings();

        DriverEarningsDTO john = earnings.stream()
            .filter(dto -> "Ivan Petrenko".equals(dto.getDriverName()))
            .findFirst()
            .orElse(null);
        boolean actualResult = earnings.size() == 2
            && john != null
            && john.getEarnings() == 500.0;
        boolean expectedResult = true;
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void getMostProfitable_WhenDriversExist_ReturnsTopDriverSummary() {
        when(driverService.findAll()).thenReturn(Arrays.asList(driver1, driver2));

        String actualResult = statsService.getMostProfitable();
        String expectedResult = "Mykhailo Kovalenko (€750.00)";

        assertEquals(expectedResult, actualResult);
    }

    @Test
    void getMostProfitable_WhenNoDrivers_ReturnsNoDataMessage() {
        when(driverService.findAll()).thenReturn(List.of());

        String actualResult = statsService.getMostProfitable();
        String expectedResult = "No data available";

        assertEquals(expectedResult, actualResult);
    }

    @Test
    void getAllStats_WhenDataAvailable_ReturnsAllStatSections() {
        Object[] stat = new Object[]{"Ivan Petrenko", 5L, 5000.0};
        List<Object[]> statsList = new ArrayList<>();
        statsList.add(stat);
        when(tripDataService.findStatsByDriver()).thenReturn(statsList);
        when(tripDataService.findAll()).thenReturn(Arrays.asList(completedTrip1));
        when(driverService.findAll()).thenReturn(Arrays.asList(driver1));

        StatsSummaryDTO allStats = statsService.getAllStats();

        boolean actualResult = allStats.getDriverPerformance() != null
            && allStats.getCargoByDestination() != null
            && allStats.getDriverEarnings() != null
            && allStats.getMostProfitableDriver() != null;
        boolean expectedResult = true;
        assertEquals(expectedResult, actualResult);
    }
}
