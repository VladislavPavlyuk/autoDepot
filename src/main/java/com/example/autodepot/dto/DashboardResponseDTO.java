package com.example.autodepot.dto;

import java.util.List;

public class DashboardResponseDTO {
    private List<DashboardStatDTO> stats;
    private List<DashboardOrderDTO> orders;
    private List<DashboardTripDTO> trips;
    private List<String> activity;
    private List<DriverPerformanceDTO> driverPerformance;

    public List<DashboardStatDTO> getStats() {
        return stats;
    }

    public void setStats(List<DashboardStatDTO> stats) {
        this.stats = stats;
    }

    public List<DashboardOrderDTO> getOrders() {
        return orders;
    }

    public void setOrders(List<DashboardOrderDTO> orders) {
        this.orders = orders;
    }

    public List<DashboardTripDTO> getTrips() {
        return trips;
    }

    public void setTrips(List<DashboardTripDTO> trips) {
        this.trips = trips;
    }

    public List<String> getActivity() {
        return activity;
    }

    public void setActivity(List<String> activity) {
        this.activity = activity;
    }

    public List<DriverPerformanceDTO> getDriverPerformance() {
        return driverPerformance;
    }

    public void setDriverPerformance(List<DriverPerformanceDTO> driverPerformance) {
        this.driverPerformance = driverPerformance;
    }
}
