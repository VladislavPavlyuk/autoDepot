package com.example.autodepot.service.data;

import com.example.autodepot.entity.Car;
import com.example.autodepot.repository.CarRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CarServiceImpl implements CarService {
    private final CarRepository carRepository;

    public CarServiceImpl(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    @Override
    public List<Car> findAvailableCars() {
        return carRepository.findByIsBrokenFalse();
    }

    @Override
    public long count() {
        return carRepository.count();
    }

    @Override
    public Car save(Car car) {
        return carRepository.save(car);
    }
}
