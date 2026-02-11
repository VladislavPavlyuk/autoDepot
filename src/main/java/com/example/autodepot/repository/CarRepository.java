package com.example.autodepot.repository;

import com.example.autodepot.entity.Car;

import java.util.List;
import java.util.Optional;

public interface CarRepository {

    List<Car> findByIsBrokenFalse();

    Optional<Car> findById(Long id);

    long count();

    Car save(Car car);

    void deleteAll();
}
