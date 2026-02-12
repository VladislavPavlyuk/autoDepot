package com.example.autodepot.service.data;

import com.example.autodepot.entity.Driver;
import com.example.autodepot.repository.DriverRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DriverServiceImpl implements DriverService {
    private final DriverRepository driverRepository;

    public DriverServiceImpl(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    @Override
    public List<Driver> findAvailableDrivers() {
        return driverRepository.findByIsAvailableTrue();
    }

    @Override
    public List<Driver> findAll() {
        return driverRepository.findAll();
    }

    @Override
    public java.util.Optional<Driver> findById(Long id) {
        return driverRepository.findById(id);
    }

    @Override
    public long count() {
        return driverRepository.count();
    }

    @Override
    @Transactional
    public Driver save(Driver driver) {
        return driverRepository.save(driver);
    }
}
