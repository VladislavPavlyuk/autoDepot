package com.example.autodepot.service;

import com.example.autodepot.entity.Car;
import com.example.autodepot.entity.Driver;
import com.example.autodepot.entity.Order;
import com.example.autodepot.entity.Trip;
import com.example.autodepot.repository.CarRepository;
import com.example.autodepot.repository.DriverRepository;
import com.example.autodepot.repository.OrderRepository;
import com.example.autodepot.repository.TripRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class TripService {
    private final DriverRepository driverRepo;
    private final CarRepository carRepo;
    private final TripRepository tripRepo;
    private final OrderRepository orderRepository;
    private final Random random = new Random();
    private static final String LOG_FILE = "trips.log";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Minimum experience required for different cargo types
    private static final Map<String, Integer> CARGO_TYPE_REQUIREMENTS = Map.of(
        "FRAGILE", 5,
        "HAZARDOUS", 10,
        "OVERSIZED", 7,
        "STANDARD", 1
    );

    public TripService(DriverRepository driverRepo, CarRepository carRepo, 
                      TripRepository tripRepo, OrderRepository orderRepository) {
        this.driverRepo = driverRepo;
        this.carRepo = carRepo;
        this.tripRepo = tripRepo;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public void createTrip(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Select driver based on experience required for cargo type
        Integer requiredExperience = CARGO_TYPE_REQUIREMENTS.getOrDefault(
            order.getCargoType().toUpperCase(), 1);
        
        List<Driver> availableDrivers = driverRepo.findByIsAvailableTrue().stream()
            .filter(d -> d.getExperience() >= requiredExperience)
            .sorted(Comparator.comparing(Driver::getExperience).reversed())
            .collect(Collectors.toList());
            
        if (availableDrivers.isEmpty()) {
            throw new RuntimeException("No available drivers with required experience: " + requiredExperience);
        }

        // Optimize car selection: minimize difference between capacity and weight
        List<Car> availableCars = carRepo.findByIsBrokenFalse().stream()
            .filter(c -> c.getCapacity() >= order.getWeight())
            .sorted(Comparator.comparing(c -> Math.abs(c.getCapacity() - order.getWeight())))
            .collect(Collectors.toList());
            
        if (availableCars.isEmpty()) {
            throw new RuntimeException("No car with sufficient capacity");
        }

        Driver driver = availableDrivers.get(0);
        Car car = availableCars.get(0);

        Trip trip = new Trip(order, driver, car);
        trip.setStatus(Trip.TripStatus.IN_PROGRESS);
        tripRepo.save(trip);

        driver.setAvailable(false);
        driverRepo.save(driver);

        logTrip("TRIP_STARTED", trip);
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

        logTrip("BREAKDOWN", trip);
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

        logTrip("REPAIR_REQUESTED", trip);
    }

    @Transactional
    public void completeTrip(Long tripId, String carStatus) {
        Trip trip = tripRepo.findById(tripId)
            .orElseThrow(() -> new RuntimeException("Trip not found: " + tripId));

        if (trip.getStatus() != Trip.TripStatus.IN_PROGRESS) {
            throw new RuntimeException("Trip is not in progress");
        }

        // Calculate payment: base rate * weight * cargo type multiplier
        double baseRate = 100.0;
        Map<String, Double> cargoMultipliers = Map.of(
            "FRAGILE", 1.5,
            "HAZARDOUS", 2.0,
            "OVERSIZED", 1.3,
            "STANDARD", 1.0
        );
        double multiplier = cargoMultipliers.getOrDefault(
            trip.getOrder().getCargoType().toUpperCase(), 1.0);
        double payment = baseRate * trip.getOrder().getWeight() * multiplier;

        trip.setStatus(Trip.TripStatus.COMPLETED);
        trip.setEndTime(LocalDateTime.now());
        trip.setPayment(payment);
        trip.setCarStatusAfterTrip(carStatus);
        tripRepo.save(trip);

        // Release driver and car
        Driver driver = trip.getDriver();
        driver.setAvailable(true);
        driver.addEarnings(payment);
        driverRepo.save(driver);

        Car car = trip.getCar();
        if ("BROKEN".equalsIgnoreCase(carStatus)) {
            car.setBroken(true);
        }
        carRepo.save(car);

        logTrip("TRIP_COMPLETED", trip);
    }

    @Transactional
    public void simulateRandomBreakdown() {
        List<Trip> activeTrips = tripRepo.findAll().stream()
            .filter(t -> t.getStatus() == Trip.TripStatus.IN_PROGRESS)
            .collect(Collectors.toList());

        if (!activeTrips.isEmpty() && random.nextDouble() < 0.1) { // 10% probability
            Trip trip = activeTrips.get(random.nextInt(activeTrips.size()));
            processBreakdown(trip.getId());
        }
    }

    private void logTrip(String event, Trip trip) {
        try {
            Path logPath = Paths.get(LOG_FILE);
            if (!Files.exists(logPath)) {
                Files.createFile(logPath);
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
                writer.printf("[%s] %s - Trip ID: %d, Driver: %s, Car ID: %d, Order: %s -> %s, Weight: %.2f kg, Status: %s%n",
                    LocalDateTime.now().format(FORMATTER),
                    event,
                    trip.getId(),
                    trip.getDriver().getName(),
                    trip.getCar().getId(),
                    trip.getOrder().getCargoType(),
                    trip.getOrder().getDestination(),
                    trip.getOrder().getWeight(),
                    trip.getStatus());
                
                if (trip.getPayment() != null) {
                    writer.printf("  Payment: %.2f, Car Status: %s%n", 
                        trip.getPayment(), trip.getCarStatusAfterTrip());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }
}
