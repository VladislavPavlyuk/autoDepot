package com.example.autodepot.service;

import com.example.autodepot.dto.OrderDTO;

public interface OrderApplicationService {

    void createOrder(OrderDTO orderDTO);
}
