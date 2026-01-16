package com.example.autodepot.service.simulation;

import com.example.autodepot.entity.Trip;

import java.util.List;
import java.util.Optional;

public interface BreakdownSimulator {
    Optional<Trip> chooseTripToBreak(List<Trip> activeTrips);
}
