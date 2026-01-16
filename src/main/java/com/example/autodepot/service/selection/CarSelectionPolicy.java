package com.example.autodepot.service.selection;

import com.example.autodepot.entity.Car;
import com.example.autodepot.entity.Order;

import java.util.List;
import java.util.Optional;

public interface CarSelectionPolicy {
    Optional<Car> selectCar(Order order, List<Car> availableCars);
}
