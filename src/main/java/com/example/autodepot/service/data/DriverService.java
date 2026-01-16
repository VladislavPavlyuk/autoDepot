package com.example.autodepot.service.data;

import com.example.autodepot.entity.Driver;
import com.example.autodepot.repository.DriverRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DriverService {
    private final DriverRepository driverRepository;

    public DriverService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    public List<Driver> findAvailableDrivers() {
        return driverRepository.findByIsAvailableTrue();
    }

    public List<Driver> findAll() {
        return driverRepository.findAll();
    }

    public long count() {
        return driverRepository.count();
    }

    public Driver save(Driver driver) {
        return driverRepository.save(driver);
    }
}
