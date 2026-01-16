package com.example.autodepot.service.selection;

import com.example.autodepot.entity.Car;
import com.example.autodepot.entity.Order;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class CapacityBasedCarSelectionPolicy implements CarSelectionPolicy {
    @Override
    public Optional<Car> selectCar(Order order, List<Car> availableCars) {
        return availableCars.stream()
            .filter(c -> c.getCapacity() >= order.getWeight())
            .sorted(Comparator.comparing(c -> Math.abs(c.getCapacity() - order.getWeight())))
            .findFirst();
    }
}
