package com.example.autodepot.service.impl;

import com.example.autodepot.dto.TripAssignDTO;
import com.example.autodepot.dto.TripBreakdownDTO;
import com.example.autodepot.dto.TripCompleteDTO;
import com.example.autodepot.dto.TripRepairDTO;
import com.example.autodepot.entity.Car;
import com.example.autodepot.entity.Driver;
import com.example.autodepot.entity.Order;
import com.example.autodepot.entity.Trip;
import com.example.autodepot.mapper.TripCommandMapper;
import com.example.autodepot.service.TripService;
import com.example.autodepot.service.command.TripAssignCommand;
import com.example.autodepot.service.command.TripBreakdownCommand;
import com.example.autodepot.service.command.TripCompleteCommand;
import com.example.autodepot.service.command.TripRepairCommand;
import com.example.autodepot.exception.BadRequestException;
import com.example.autodepot.exception.NotFoundException;
import com.example.autodepot.service.data.CarService;
import com.example.autodepot.service.data.DriverService;
import com.example.autodepot.service.data.OrderService;
import com.example.autodepot.service.data.TripDataService;
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
public class TripServiceImpl implements TripService {

    private final DriverService driverService;
    private final CarService carService;
    private final TripDataService tripDataService;
    private final OrderService orderService;
    private final DriverSelectionPolicy driverSelectionPolicy;
    private final CarSelectionPolicy carSelectionPolicy;
    private final PaymentCalculator paymentCalculator;
    private final TripEventLogger tripEventLogger;
    private final BreakdownSimulator breakdownSimulator;
    private final TripCommandMapper tripCommandMapper;

    public TripServiceImpl(DriverService driverService, CarService carService,
                           TripDataService tripDataService, OrderService orderService,
                           DriverSelectionPolicy driverSelectionPolicy,
                           CarSelectionPolicy carSelectionPolicy,
                           PaymentCalculator paymentCalculator,
                           TripEventLogger tripEventLogger,
                           BreakdownSimulator breakdownSimulator,
                           TripCommandMapper tripCommandMapper) {
        this.driverService = driverService;
        this.carService = carService;
        this.tripDataService = tripDataService;
        this.orderService = orderService;
        this.driverSelectionPolicy = driverSelectionPolicy;
        this.carSelectionPolicy = carSelectionPolicy;
        this.paymentCalculator = paymentCalculator;
        this.tripEventLogger = tripEventLogger;
        this.breakdownSimulator = breakdownSimulator;
        this.tripCommandMapper = tripCommandMapper;
    }

    @Override
    @Transactional
    public void createTrip(TripAssignDTO assignDTO) {
        TripAssignCommand command = tripCommandMapper.toCommand(assignDTO);
        Order order = orderService.findById(command.getOrderId())
            .orElseThrow(() -> new NotFoundException("Order not found: " + command.getOrderId()));

        List<Driver> availableDrivers = driverService.findAvailableDrivers();
        Driver driver = driverSelectionPolicy.selectDriver(order, availableDrivers)
            .orElseThrow(() -> new NotFoundException("No available drivers for order: " + command.getOrderId()));

        List<Car> availableCars = carService.findAvailableCars();
        Car car = carSelectionPolicy.selectCar(order, availableCars)
            .orElseThrow(() -> new NotFoundException("No car with sufficient capacity"));

        Trip trip = new Trip(order, driver, car);
        trip.setStatus(Trip.TripStatus.IN_PROGRESS);
        tripDataService.save(trip);

        driver.setAvailable(false);
        driverService.save(driver);

        tripEventLogger.logEvent("TRIP_STARTED", trip);
    }

    @Override
    @Transactional
    public void processBreakdown(TripBreakdownDTO breakdownDTO) {
        TripBreakdownCommand command = tripCommandMapper.toCommand(breakdownDTO);
        Trip trip = tripDataService.findById(command.getTripId())
            .orElseThrow(() -> new NotFoundException("Trip not found: " + command.getTripId()));

        Car car = trip.getCar();
        car.setBroken(true);
        carService.save(car);

        trip.setStatus(Trip.TripStatus.BROKEN);
        tripDataService.save(trip);

        tripEventLogger.logEvent("BREAKDOWN", trip);
    }

    @Override
    @Transactional
    public void requestRepair(TripRepairDTO repairDTO) {
        TripRepairCommand command = tripCommandMapper.toCommand(repairDTO);
        Trip trip = tripDataService.findById(command.getTripId())
            .orElseThrow(() -> new NotFoundException("Trip not found: " + command.getTripId()));

        if (trip.getStatus() != Trip.TripStatus.BROKEN) {
            throw new BadRequestException("Trip is not broken");
        }

        trip.setStatus(Trip.TripStatus.REPAIR_REQUESTED);
        tripDataService.save(trip);

        tripEventLogger.logEvent("REPAIR_REQUESTED", trip);
    }

    @Override
    @Transactional
    public void completeTrip(TripCompleteDTO completeDTO) {
        TripCompleteCommand command = tripCommandMapper.toCommand(completeDTO);
        Trip trip = tripDataService.findById(command.getTripId())
            .orElseThrow(() -> new NotFoundException("Trip not found: " + command.getTripId()));

        if (trip.getStatus() != Trip.TripStatus.IN_PROGRESS) {
            throw new BadRequestException("Trip is not in progress");
        }

        double payment = paymentCalculator.calculatePayment(trip.getOrder());

        trip.setStatus(Trip.TripStatus.COMPLETED);
        trip.setEndTime(LocalDateTime.now());
        trip.setPayment(payment);
        trip.setCarStatusAfterTrip(command.getCarStatus());
        tripDataService.save(trip);

        Driver driver = trip.getDriver();
        driver.setAvailable(true);
        driver.addEarnings(payment);
        driverService.save(driver);

        Car car = trip.getCar();
        if ("BROKEN".equalsIgnoreCase(command.getCarStatus())) {
            car.setBroken(true);
        }
        carService.save(car);

        tripEventLogger.logEvent("TRIP_COMPLETED", trip);
    }

    @Override
    @Transactional
    public void simulateRandomBreakdown() {
        List<Trip> activeTrips = tripDataService.findAll().stream()
            .filter(t -> t.getStatus() == Trip.TripStatus.IN_PROGRESS)
            .collect(Collectors.toList());

        breakdownSimulator.chooseTripToBreak(activeTrips)
            .map(tripCommandMapper::toBreakdownDto)
            .ifPresent(this::processBreakdown);
    }
}
