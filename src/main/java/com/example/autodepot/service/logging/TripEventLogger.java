package com.example.autodepot.service.logging;

import com.example.autodepot.entity.Trip;

public interface TripEventLogger {
    void logEvent(String event, Trip trip);
}
