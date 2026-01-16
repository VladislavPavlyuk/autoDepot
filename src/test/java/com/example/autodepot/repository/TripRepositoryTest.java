package com.example.autodepot.repository;

import com.example.autodepot.AbstractPostgresTest;
import com.example.autodepot.entity.Car;
import com.example.autodepot.entity.Driver;
import com.example.autodepot.entity.Order;
import com.example.autodepot.entity.Trip;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void testSaveAndFindTrip() {
        Driver driver = driverRepository.save(new Driver("John Smith", 5));
        Car car = carRepository.save(new Car(2000.0));
        Order order = orderRepository.save(new Order("New York", "STANDARD", 1000.0));

        Trip trip = new Trip(order, driver, car);
        trip.setStatus(Trip.TripStatus.IN_PROGRESS);
        Trip saved = tripRepository.save(trip);
        Trip found = tripRepository.findById(saved.getId()).orElse(null);

        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());
        assertEquals(order.getId(), found.getOrder().getId());
        assertEquals(driver.getId(), found.getDriver().getId());
        assertEquals(car.getId(), found.getCar().getId());
    }

    @Test
    void testExistsByOrderId() {
        Driver driver = driverRepository.save(new Driver("John Smith", 5));
        Car car = carRepository.save(new Car(2000.0));
        Order order = orderRepository.save(new Order("New York", "STANDARD", 1000.0));

        Trip trip = new Trip(order, driver, car);
        trip.setStatus(Trip.TripStatus.IN_PROGRESS);
        tripRepository.save(trip);

        assertTrue(tripRepository.existsByOrderId(order.getId()));
        assertFalse(tripRepository.existsByOrderId(999999L));
    }

    @Test
    void testFindStatsByDriver() {
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

        assertNotNull(stats);
        assertEquals(1, stats.size());
        assertEquals("John Smith", stats.get(0)[0]);
        assertEquals(2L, stats.get(0)[1]);
        assertEquals(3000.0, ((Number) stats.get(0)[2]).doubleValue());
    }
}
