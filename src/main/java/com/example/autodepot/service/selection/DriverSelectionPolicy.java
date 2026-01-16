package com.example.autodepot.service.selection;

import com.example.autodepot.entity.Driver;
import com.example.autodepot.entity.Order;

import java.util.List;
import java.util.Optional;

public interface DriverSelectionPolicy {
    Optional<Driver> selectDriver(Order order, List<Driver> availableDrivers);
}
