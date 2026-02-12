package com.example.autodepot.service.data;

import com.example.autodepot.entity.Driver;

import java.util.List;
import java.util.Optional;

public interface DriverService {
    List<Driver> findAvailableDrivers();

    List<Driver> findAll();

    Optional<Driver> findById(Long id);

    long count();

    Driver save(Driver driver);
}
