package com.example.autodepot.repository;

import com.example.autodepot.AbstractPostgresTest;
import com.example.autodepot.entity.Car;
import com.example.autodepot.entity.Driver;
import com.example.autodepot.entity.Order;
import com.example.autodepot.entity.Trip;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class TripRepositoryTest extends AbstractPostgresTest {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        tripRepository.deleteAll();
        orderRepository.deleteAll();
        driverRepository.deleteAll();
        carRepository.deleteAll();
    }

    @Test
    void saveAndFindTrip_WhenTripSaved_ReturnsMatchingTrip() {
        Driver driver = driverRepository.save(new Driver("John Smith", 5));
        Car car = carRepository.save(new Car(2000.0));
        Order order = orderRepository.save(new Order("New York", "STANDARD", 1000.0));

        Trip trip = new Trip(order, driver, car);
        trip.setStatus(Trip.TripStatus.IN_PROGRESS);
        Trip saved = tripRepository.save(trip);
        Trip found = tripRepository.findById(saved.getId()).orElse(null);
        boolean actualResult = found != null
            && saved.getId().equals(found.getId())
            && order.getId().equals(found.getOrder().getId())
            && driver.getId().equals(found.getDriver().getId())
            && car.getId().equals(found.getCar().getId());
        boolean expectedResult = true;
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void existsByOrderId_WhenOrderLinkedToTrip_ReturnsTrue() {
        Driver driver = driverRepository.save(new Driver("John Smith", 5));
        Car car = carRepository.save(new Car(2000.0));
        Order order = orderRepository.save(new Order("New York", "STANDARD", 1000.0));

        Trip trip = new Trip(order, driver, car);
        trip.setStatus(Trip.TripStatus.IN_PROGRESS);
        tripRepository.save(trip);

        boolean actualResult = tripRepository.existsByOrderId(order.getId())
            && !tripRepository.existsByOrderId(999999L);
        boolean expectedResult = true;
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void findStatsByDriver_WhenTripsExist_ReturnsDriverTripCount() {
        Driver driver = driverRepository.save(new Driver("John Smith", 5));
        Car car = carRepository.save(new Car(2000.0));
        Order order1 = orderRepository.save(new Order("New York", "STANDARD", 1000.0));
        Order order2 = orderRepository.save(new Order("Chicago", "STANDARD", 2000.0));

        Trip trip1 = new Trip(order1, driver, car);
        trip1.setStatus(Trip.TripStatus.COMPLETED);
        tripRepository.save(trip1);

        Trip trip2 = new Trip(order2, driver, car);
        trip2.setStatus(Trip.TripStatus.COMPLETED);
        tripRepository.save(trip2);

        List<Object[]> stats = tripRepository.findStatsByDriver();
        long actualResult = stats == null ? 0L : (long) stats.get(0)[1];
        long expectedResult = 2L;
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void findStatsByDriver_WhenTripsExist_ReturnsDriverName() {
        Driver driver = driverRepository.save(new Driver("John Smith", 5));
        Car car = carRepository.save(new Car(2000.0));
        Order order1 = orderRepository.save(new Order("New York", "STANDARD", 1000.0));
        Order order2 = orderRepository.save(new Order("Chicago", "STANDARD", 2000.0));

        Trip trip1 = new Trip(order1, driver, car);
        trip1.setStatus(Trip.TripStatus.COMPLETED);
        tripRepository.save(trip1);

        Trip trip2 = new Trip(order2, driver, car);
        trip2.setStatus(Trip.TripStatus.COMPLETED);
        tripRepository.save(trip2);

        List<Object[]> stats = tripRepository.findStatsByDriver();
        String actualResult = stats == null ? null : (String) stats.get(0)[0];
        String expectedResult = "John Smith";
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void findStatsByDriver_WhenTripsExist_ReturnsDriverTotalWeight() {
        Driver driver = driverRepository.save(new Driver("John Smith", 5));
        Car car = carRepository.save(new Car(2000.0));
        Order order1 = orderRepository.save(new Order("New York", "STANDARD", 1000.0));
        Order order2 = orderRepository.save(new Order("Chicago", "STANDARD", 2000.0));

        Trip trip1 = new Trip(order1, driver, car);
        trip1.setStatus(Trip.TripStatus.COMPLETED);
        tripRepository.save(trip1);

        Trip trip2 = new Trip(order2, driver, car);
        trip2.setStatus(Trip.TripStatus.COMPLETED);
        tripRepository.save(trip2);

        List<Object[]> stats = tripRepository.findStatsByDriver();
        double actualResult = stats == null ? 0.0 : ((Number) stats.get(0)[2]).doubleValue();
        double expectedResult = 3000.0;
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void findStatsByDriver_WhenNoTrips_ReturnsEmptyStats() {
        List<Object[]> stats = tripRepository.findStatsByDriver();
        int actualResult = stats == null ? 0 : stats.size();
        int expectedResult = 0;
        assertEquals(expectedResult, actualResult);
    }
}
