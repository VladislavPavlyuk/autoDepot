package com.example.autodepot.service.data;

import com.example.autodepot.entity.Car;

import java.util.List;

public interface CarService {
    List<Car> findAvailableCars();

    long count();

    Car save(Car car);
}
