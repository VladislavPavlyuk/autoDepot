package com.example.autodepot.service.data;

import com.example.autodepot.entity.Car;
import com.example.autodepot.repository.CarRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CarService {
    private final CarRepository carRepository;

    public CarService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    public List<Car> findAvailableCars() {
        return carRepository.findByIsBrokenFalse();
    }

    public long count() {
        return carRepository.count();
    }

    public Car save(Car car) {
        return carRepository.save(car);
    }
}
