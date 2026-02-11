package com.example.autodepot.repository;

import com.example.autodepot.entity.Driver;

import java.util.List;
import java.util.Optional;

public interface DriverRepository {

    List<Driver> findByIsAvailableTrue();

    Optional<Driver> findById(Long id);

    List<Driver> findAll();

    long count();

    Driver save(Driver driver);

    void deleteAll();
}
