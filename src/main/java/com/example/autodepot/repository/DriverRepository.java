package com.example.autodepot.repository;

import com.example.autodepot.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    List<Driver> findByIsAvailableTrue();
    Optional<Driver> findByIdAndIsAvailableTrue(Long id);
}
