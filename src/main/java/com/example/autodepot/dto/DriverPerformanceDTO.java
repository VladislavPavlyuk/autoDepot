package com.example.autodepot.dto;

import java.util.List;

public class DriverPerformanceDTO {
    private Long driverId;
    private String driverName;
    private long tripCount;
    private double totalWeight;
    private double earnings;
    private List<String> licenseCategories;
    private Integer experience;
    private Integer licenseYear;

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public long getTripCount() {
        return tripCount;
    }

    public void setTripCount(long tripCount) {
        this.tripCount = tripCount;
    }

    public double getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(double totalWeight) {
        this.totalWeight = totalWeight;
    }

    public double getEarnings() {
        return earnings;
    }

    public void setEarnings(double earnings) {
        this.earnings = earnings;
    }

    public List<String> getLicenseCategories() {
        return licenseCategories;
    }

    public void setLicenseCategories(List<String> licenseCategories) {
        this.licenseCategories = licenseCategories;
    }

    public Integer getExperience() {
        return experience;
    }

    public void setExperience(Integer experience) {
        this.experience = experience;
    }

    public Integer getLicenseYear() {
        return licenseYear;
    }

    public void setLicenseYear(Integer licenseYear) {
        this.licenseYear = licenseYear;
    }
}
