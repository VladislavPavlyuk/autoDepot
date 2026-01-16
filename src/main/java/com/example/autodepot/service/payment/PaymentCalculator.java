package com.example.autodepot.service.payment;

import com.example.autodepot.entity.Order;

public interface PaymentCalculator {
    double calculatePayment(Order order);
}
