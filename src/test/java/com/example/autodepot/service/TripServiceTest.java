package com.example.autodepot.service;

import com.example.autodepot.dto.TripAssignDTO;
import com.example.autodepot.dto.TripBreakdownDTO;
import com.example.autodepot.dto.TripCompleteDTO;
import com.example.autodepot.dto.TripRepairDTO;
import com.example.autodepot.entity.Car;
import com.example.autodepot.entity.Driver;
import com.example.autodepot.entity.Order;
import com.example.autodepot.entity.Trip;
import com.example.autodepot.mapper.TripCommandMapper;
import com.example.autodepot.service.command.TripAssignCommand;
import com.example.autodepot.service.command.TripBreakdownCommand;
import com.example.autodepot.service.command.TripCompleteCommand;
import com.example.autodepot.service.command.TripRepairCommand;
import com.example.autodepot.service.data.CarService;
import com.example.autodepot.service.data.DriverService;
import com.example.autodepot.service.data.OrderService;
import com.example.autodepot.service.data.TripDataService;
import com.example.autodepot.service.impl.TripServiceImpl;
import com.example.autodepot.service.logging.TripEventLogger;
import com.example.autodepot.service.payment.PaymentCalculator;
import com.example.autodepot.service.selection.CarSelectionPolicy;
import com.example.autodepot.service.selection.DriverSelectionPolicy;
import com.example.autodepot.service.simulation.BreakdownSimulator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private DriverService driverService;

    @Mock
    private CarService carService;

    @Mock
    private TripDataService tripDataService;

    @Mock
    private OrderService orderService;

    @Mock
    private DriverSelectionPolicy driverSelectionPolicy;

    @Mock
    private CarSelectionPolicy carSelectionPolicy;

    @Mock
    private PaymentCalculator paymentCalculator;

    @Mock
    private TripEventLogger tripEventLogger;

    @Mock
    private BreakdownSimulator breakdownSimulator;

    @Mock
    private TripCommandMapper tripCommandMapper;

    @InjectMocks
    private TripServiceImpl tripService;

    private Order testOrder;
    private Driver testDriver;
    private Car testCar;

    @BeforeEach
    void setUp() {
        testOrder = new Order("Berlin", "STANDARD", 1000.0);
        testOrder.setId(1L);

        testDriver = new Driver("Ivan Petrenko", 5);
        testDriver.setId(1L);
        testDriver.setAvailable(true);

        testCar = new Car(2000.0);
        testCar.setId(1L);
        testCar.setBroken(false);
    }

    @Test
    void createTrip_WhenValidOrderAndResourcesExist_CompletesSuccessfully() {
        when(orderService.findById(1L)).thenReturn(Optional.of(testOrder));
        when(driverService.findAvailableDrivers()).thenReturn(List.of(testDriver));
        when(carService.findAvailableCars()).thenReturn(List.of(testCar));
        when(driverSelectionPolicy.selectDriver(eq(testOrder), anyList()))
            .thenReturn(Optional.of(testDriver));
        when(carSelectionPolicy.selectCar(eq(testOrder), anyList()))
            .thenReturn(Optional.of(testCar));
        when(tripDataService.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(driverService.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TripAssignDTO assignDTO = new TripAssignDTO();
        assignDTO.setOrderId(1L);
        TripAssignCommand assignCommand = new TripAssignCommand();
        assignCommand.setOrderId(1L);
        when(tripCommandMapper.toCommand(assignDTO)).thenReturn(assignCommand);
        boolean actualResult = true;
        try {
            tripService.createTrip(assignDTO);
        } catch (RuntimeException ex) {
            actualResult = false;
        }
        boolean expectedResult = true;
        assertEquals(expectedResult, actualResult);
        verify(orderService, times(1)).findById(1L);
        verify(driverService, times(1)).findAvailableDrivers();
        verify(carService, times(1)).findAvailableCars();
        verify(tripDataService, times(1)).save(any(Trip.class));
        verify(driverService, times(1)).save(any(Driver.class));
    }

    @Test
    void createTrip_WhenOrderMissing_ThrowsNotFoundMessage() {
        when(orderService.findById(1L)).thenReturn(Optional.empty());

        TripAssignDTO assignDTO = new TripAssignDTO();
        assignDTO.setOrderId(1L);
        TripAssignCommand assignCommand = new TripAssignCommand();
        assignCommand.setOrderId(1L);
        when(tripCommandMapper.toCommand(assignDTO)).thenReturn(assignCommand);
        String actualResult;
        try {
            tripService.createTrip(assignDTO);
            actualResult = "no exception";
        } catch (RuntimeException ex) {
            actualResult = ex.getMessage();
        }
        String expectedResult = "Order not found: 1";
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void createTrip_WhenNoAvailableDrivers_ThrowsNoDriversMessage() {
        when(orderService.findById(1L)).thenReturn(Optional.of(testOrder));
        when(driverService.findAvailableDrivers()).thenReturn(new ArrayList<>());
        when(driverSelectionPolicy.selectDriver(eq(testOrder), anyList()))
            .thenReturn(Optional.empty());

        TripAssignDTO assignDTO = new TripAssignDTO();
        assignDTO.setOrderId(1L);
        TripAssignCommand assignCommand = new TripAssignCommand();
        assignCommand.setOrderId(1L);
        when(tripCommandMapper.toCommand(assignDTO)).thenReturn(assignCommand);
        String actualResult;
        try {
            tripService.createTrip(assignDTO);
            actualResult = "no exception";
        } catch (RuntimeException ex) {
            actualResult = ex.getMessage();
        }
        String expectedResult = "No available drivers for order: 1";
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void createTrip_WhenHazardousOrder_SelectsSeniorDriver() {
        Order hazardousOrder = new Order("Paris", "HAZARDOUS", 1000.0);
        hazardousOrder.setId(2L);
        
        Driver juniorDriver = new Driver("Andrii Melnyk", 3);
        juniorDriver.setId(2L);
        juniorDriver.setAvailable(true);
        
        Driver seniorDriver = new Driver("Oleksandr Shevchenko", 12);
        seniorDriver.setId(3L);
        seniorDriver.setAvailable(true);

        when(orderService.findById(2L)).thenReturn(Optional.of(hazardousOrder));
        when(driverService.findAvailableDrivers()).thenReturn(List.of(juniorDriver, seniorDriver));
        when(carService.findAvailableCars()).thenReturn(List.of(testCar));
        when(driverSelectionPolicy.selectDriver(eq(hazardousOrder), anyList()))
            .thenReturn(Optional.of(seniorDriver));
        when(carSelectionPolicy.selectCar(eq(hazardousOrder), anyList()))
            .thenReturn(Optional.of(testCar));
        when(tripDataService.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(driverService.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TripAssignDTO assignDTO = new TripAssignDTO();
        assignDTO.setOrderId(2L);
        TripAssignCommand assignCommand = new TripAssignCommand();
        assignCommand.setOrderId(2L);
        when(tripCommandMapper.toCommand(assignDTO)).thenReturn(assignCommand);
        tripService.createTrip(assignDTO);

        ArgumentCaptor<Trip> tripCaptor = ArgumentCaptor.forClass(Trip.class);
        verify(tripDataService, times(1)).save(tripCaptor.capture());
        Trip savedTrip = tripCaptor.getValue();
        boolean actualResult = savedTrip.getDriver().getExperience() >= 10;
        boolean expectedResult = true;
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void completeTrip_WhenInProgress_SetsCompletedAndPayment() {
        Trip trip = new Trip(testOrder, testDriver, testCar);
        trip.setId(1L);
        trip.setStatus(Trip.TripStatus.IN_PROGRESS);

        when(tripDataService.findById(1L)).thenReturn(Optional.of(trip));
        when(paymentCalculator.calculatePayment(eq(testOrder))).thenReturn(123.0);
        when(tripDataService.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(driverService.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(carService.save(any(Car.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TripCompleteDTO completeDTO = new TripCompleteDTO();
        completeDTO.setTripId(1L);
        completeDTO.setCarStatus("OK");
        TripCompleteCommand completeCommand = new TripCompleteCommand();
        completeCommand.setTripId(1L);
        completeCommand.setCarStatus("OK");
        when(tripCommandMapper.toCommand(completeDTO)).thenReturn(completeCommand);
        tripService.completeTrip(completeDTO);

        ArgumentCaptor<Trip> tripCaptor = ArgumentCaptor.forClass(Trip.class);
        ArgumentCaptor<Driver> driverCaptor = ArgumentCaptor.forClass(Driver.class);
        verify(tripDataService, times(1)).save(tripCaptor.capture());
        verify(driverService, times(1)).save(driverCaptor.capture());
        Trip savedTrip = tripCaptor.getValue();
        Driver savedDriver = driverCaptor.getValue();
        boolean actualResult = savedTrip.getStatus() == Trip.TripStatus.COMPLETED
            && savedTrip.getPayment() != null
            && "OK".equals(savedTrip.getCarStatusAfterTrip())
            && savedDriver.isAvailable();
        boolean expectedResult = true;
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void processBreakdown_WhenTripInProgress_SetsBrokenAndCarBroken() {
        Trip trip = new Trip(testOrder, testDriver, testCar);
        trip.setId(1L);
        trip.setStatus(Trip.TripStatus.IN_PROGRESS);

        when(tripDataService.findById(1L)).thenReturn(Optional.of(trip));
        when(tripDataService.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(carService.save(any(Car.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TripBreakdownDTO breakdownDTO = new TripBreakdownDTO();
        breakdownDTO.setTripId(1L);
        TripBreakdownCommand breakdownCommand = new TripBreakdownCommand();
        breakdownCommand.setTripId(1L);
        when(tripCommandMapper.toCommand(breakdownDTO)).thenReturn(breakdownCommand);
        tripService.processBreakdown(breakdownDTO);

        ArgumentCaptor<Trip> tripCaptor = ArgumentCaptor.forClass(Trip.class);
        ArgumentCaptor<Car> carCaptor = ArgumentCaptor.forClass(Car.class);
        verify(tripDataService, times(1)).save(tripCaptor.capture());
        verify(carService, times(1)).save(carCaptor.capture());
        Trip savedTrip = tripCaptor.getValue();
        Car savedCar = carCaptor.getValue();
        boolean actualResult = savedTrip.getStatus() == Trip.TripStatus.BROKEN
            && savedCar.isBroken();
        boolean expectedResult = true;
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void requestRepair_WhenTripBroken_SetsRepairRequested() {
        Trip trip = new Trip(testOrder, testDriver, testCar);
        trip.setId(1L);
        trip.setStatus(Trip.TripStatus.BROKEN);

        when(tripDataService.findById(1L)).thenReturn(Optional.of(trip));
        when(tripDataService.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TripRepairDTO repairDTO = new TripRepairDTO();
        repairDTO.setTripId(1L);
        TripRepairCommand repairCommand = new TripRepairCommand();
        repairCommand.setTripId(1L);
        when(tripCommandMapper.toCommand(repairDTO)).thenReturn(repairCommand);
        tripService.requestRepair(repairDTO);

        ArgumentCaptor<Trip> tripCaptor = ArgumentCaptor.forClass(Trip.class);
        verify(tripDataService, times(1)).save(tripCaptor.capture());
        Trip savedTrip = tripCaptor.getValue();
        boolean actualResult = savedTrip.getStatus() == Trip.TripStatus.REPAIR_REQUESTED;
        boolean expectedResult = true;
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void confirmRepairComplete_WhenTripRepairRequested_SetsInProgressAndCarFixed() {
        Car car = new Car(1000.0);
        car.setId(1L);
        car.setBroken(true);
        Trip trip = new Trip(testOrder, testDriver, car);
        trip.setId(1L);
        trip.setStatus(Trip.TripStatus.REPAIR_REQUESTED);

        when(tripDataService.findById(1L)).thenReturn(Optional.of(trip));
        when(tripDataService.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(carService.save(any(Car.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TripRepairDTO repairDTO = new TripRepairDTO();
        repairDTO.setTripId(1L);
        TripRepairCommand repairCommand = new TripRepairCommand();
        repairCommand.setTripId(1L);
        when(tripCommandMapper.toCommand(repairDTO)).thenReturn(repairCommand);
        tripService.confirmRepairComplete(repairDTO);

        ArgumentCaptor<Car> carCaptor = ArgumentCaptor.forClass(Car.class);
        verify(carService).save(carCaptor.capture());
        assertFalse(carCaptor.getValue().isBroken());

        ArgumentCaptor<Trip> tripCaptor = ArgumentCaptor.forClass(Trip.class);
        verify(tripDataService).save(tripCaptor.capture());
        assertEquals(Trip.TripStatus.IN_PROGRESS, tripCaptor.getValue().getStatus());
    }

    @Test
    void processBreakdown_WhenTripIdProvided_DelegatesToDtoMethod() {
        Trip trip = new Trip(testOrder, testDriver, testCar);
        trip.setId(1L);
        trip.setStatus(Trip.TripStatus.IN_PROGRESS);

        when(tripDataService.findById(1L)).thenReturn(Optional.of(trip));
        when(tripDataService.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(carService.save(any(Car.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TripBreakdownDTO dto = new TripBreakdownDTO();
        dto.setTripId(1L);
        when(tripCommandMapper.toBreakdownDto(1L)).thenReturn(dto);
        TripBreakdownCommand command = new TripBreakdownCommand();
        command.setTripId(1L);
        when(tripCommandMapper.toCommand(dto)).thenReturn(command);

        tripService.processBreakdown(1L);

        verify(tripCommandMapper).toBreakdownDto(1L);
        verify(tripDataService).save(any(Trip.class));
    }
}
