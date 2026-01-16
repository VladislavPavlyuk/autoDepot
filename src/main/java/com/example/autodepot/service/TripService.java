package com.example.autodepot.service;

import com.example.autodepot.entity.Car;
import com.example.autodepot.entity.Driver;
import com.example.autodepot.entity.Order;
import com.example.autodepot.entity.Trip;
import com.example.autodepot.repository.CarRepository;
import com.example.autodepot.repository.DriverRepository;
import com.example.autodepot.repository.OrderRepository;
import com.example.autodepot.repository.TripRepository;
import com.example.autodepot.service.logging.TripEventLogger;
import com.example.autodepot.service.payment.PaymentCalculator;
import com.example.autodepot.service.selection.CarSelectionPolicy;
import com.example.autodepot.service.selection.DriverSelectionPolicy;
import com.example.autodepot.service.simulation.BreakdownSimulator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TripService {
    private final DriverRepository driverRepo;
    private final CarRepository carRepo;
    private final TripRepository tripRepo;
    private final OrderRepository orderRepository;
    private final DriverSelectionPolicy driverSelectionPolicy;
    private final CarSelectionPolicy carSelectionPolicy;
    private final PaymentCalculator paymentCalculator;
    private final TripEventLogger tripEventLogger;
    private final BreakdownSimulator breakdownSimulator;

    public TripService(DriverRepository driverRepo, CarRepository carRepo,
                      TripRepository tripRepo, OrderRepository orderRepository,
                      DriverSelectionPolicy driverSelectionPolicy,
                      CarSelectionPolicy carSelectionPolicy,
                      PaymentCalculator paymentCalculator,
                      TripEventLogger tripEventLogger,
                      BreakdownSimulator breakdownSimulator) {
        this.driverRepo = driverRepo;
        this.carRepo = carRepo;
        this.tripRepo = tripRepo;
        this.orderRepository = orderRepository;
        this.driverSelectionPolicy = driverSelectionPolicy;
        this.carSelectionPolicy = carSelectionPolicy;
        this.paymentCalculator = paymentCalculator;
        this.tripEventLogger = tripEventLogger;
        this.breakdownSimulator = breakdownSimulator;
    }

    @Transactional
    public void createTrip(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        List<Driver> availableDrivers = driverRepo.findByIsAvailableTrue();
        Driver driver = driverSelectionPolicy.selectDriver(order, availableDrivers)
            .orElseThrow(() -> new RuntimeException("No available drivers for order: " + orderId));

        List<Car> availableCars = carRepo.findByIsBrokenFalse();
        Car car = carSelectionPolicy.selectCar(order, availableCars)
            .orElseThrow(() -> new RuntimeException("No car with sufficient capacity"));

        Trip trip = new Trip(order, driver, car);
        trip.setStatus(Trip.TripStatus.IN_PROGRESS);
        tripRepo.save(trip);

        driver.setAvailable(false);
        driverRepo.save(driver);

        tripEventLogger.logEvent("TRIP_STARTED", trip);
    }

    @Transactional
    public void processBreakdown(Long tripId) {
        Trip trip = tripRepo.findById(tripId)
            .orElseThrow(() -> new RuntimeException("Trip not found: " + tripId));

        Car car = trip.getCar();
        car.setBroken(true);
        carRepo.save(car);

        trip.setStatus(Trip.TripStatus.BROKEN);
        tripRepo.save(trip);

        tripEventLogger.logEvent("BREAKDOWN", trip);
    }

    @Transactional
    public void requestRepair(Long tripId) {
        Trip trip = tripRepo.findById(tripId)
            .orElseThrow(() -> new RuntimeException("Trip not found: " + tripId));

        if (trip.getStatus() != Trip.TripStatus.BROKEN) {
            throw new RuntimeException("Trip is not broken");
        }

        trip.setStatus(Trip.TripStatus.REPAIR_REQUESTED);
        tripRepo.save(trip);

        tripEventLogger.logEvent("REPAIR_REQUESTED", trip);
    }

    @Transactional
    public void completeTrip(Long tripId, String carStatus) {
        Trip trip = tripRepo.findById(tripId)
            .orElseThrow(() -> new RuntimeException("Trip not found: " + tripId));

        if (trip.getStatus() != Trip.TripStatus.IN_PROGRESS) {
            throw new RuntimeException("Trip is not in progress");
        }

        double payment = paymentCalculator.calculatePayment(trip.getOrder());

        trip.setStatus(Trip.TripStatus.COMPLETED);
        trip.setEndTime(LocalDateTime.now());
        trip.setPayment(payment);
        trip.setCarStatusAfterTrip(carStatus);
        tripRepo.save(trip);

        Driver driver = trip.getDriver();
        driver.setAvailable(true);
        driver.addEarnings(payment);
        driverRepo.save(driver);

        Car car = trip.getCar();
        if ("BROKEN".equalsIgnoreCase(carStatus)) {
            car.setBroken(true);
        }
        carRepo.save(car);

        tripEventLogger.logEvent("TRIP_COMPLETED", trip);
    }

    @Transactional
    public void simulateRandomBreakdown() {
        List<Trip> activeTrips = tripRepo.findAll().stream()
            .filter(t -> t.getStatus() == Trip.TripStatus.IN_PROGRESS)
            .collect(Collectors.toList());

        breakdownSimulator.chooseTripToBreak(activeTrips)
            .ifPresent(trip -> processBreakdown(trip.getId()));
    }
}
