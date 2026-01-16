package com.example.autodepot.service.payment;

import com.example.autodepot.entity.Order;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CargoTypePaymentCalculator implements PaymentCalculator {
    private static final double BASE_RATE = 100.0;
    private static final Map<String, Double> CARGO_MULTIPLIERS = Map.of(
        "FRAGILE", 1.5,
        "HAZARDOUS", 2.0,
        "OVERSIZED", 1.3,
        "STANDARD", 1.0
    );

    @Override
    public double calculatePayment(Order order) {
        double multiplier = CARGO_MULTIPLIERS.getOrDefault(
            order.getCargoType().toUpperCase(), 1.0);
        return BASE_RATE * order.getWeight() * multiplier;
    }
}
