package com.example.autodepot.repository;

import com.example.autodepot.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
    List<Car> findByIsBrokenFalse();
    
    @Lock(LockModeType.OPTIMISTIC)
    Optional<Car> findByIdAndIsBrokenFalse(Long id);
}
