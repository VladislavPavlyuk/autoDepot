package com.example.autodepot.service.data;

import com.example.autodepot.entity.Driver;

import java.util.List;

public interface DriverService {
    List<Driver> findAvailableDrivers();

    List<Driver> findAll();

    long count();

    Driver save(Driver driver);
}
