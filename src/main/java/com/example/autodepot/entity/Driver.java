package com.example.autodepot.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "drivers")
public class Driver {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "license_year", nullable = false)
    private Integer licenseYear;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "driver_license_categories", joinColumns = @JoinColumn(name = "driver_id"))
    @Column(name = "category", length = 2)
    private List<String> licenseCategories = new ArrayList<>();

    @Column(nullable = false)
    private boolean isAvailable = true;

    @Column(nullable = false)
    private double earnings = 0.0;

    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL)
    private List<Trip> trips;

    public Driver() {
    }

    public Driver(String name, Integer licenseYear) {
        this.name = name;
        this.licenseYear = licenseYear;
        this.licenseCategories.add("B");
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getLicenseYear() {
        return licenseYear;
    }

    public void setLicenseYear(Integer licenseYear) {
        this.licenseYear = licenseYear;
    }

    public Integer getExperience() {
        if (licenseYear == null) {
            return 0;
        }
        int currentYear = java.time.Year.now().getValue();
        return Math.max(0, currentYear - licenseYear);
    }

    public List<String> getLicenseCategories() {
        return licenseCategories != null ? licenseCategories : new ArrayList<>();
    }

    public void setLicenseCategories(List<String> licenseCategories) {
        this.licenseCategories = licenseCategories != null ? new ArrayList<>(licenseCategories) : new ArrayList<>();
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public List<Trip> getTrips() {
        return trips;
    }

    public void setTrips(List<Trip> trips) {
        this.trips = trips;
    }

    public double getEarnings() {
        return earnings;
    }

    public void setEarnings(double earnings) {
        this.earnings = earnings;
    }

    public void addEarnings(double amount) {
        this.earnings += amount;
    }
}
