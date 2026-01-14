package com.example.autodepot.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "cars")
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private double capacity;

    @Column(nullable = false)
    private boolean isBroken = false;

    @Version
    private Integer version;

    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL)
    private List<Trip> trips;

    public Car() {
    }

    public Car(double capacity) {
        this.capacity = capacity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getCapacity() {
        return capacity;
    }

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }

    public boolean isBroken() {
        return isBroken;
    }

    public void setBroken(boolean broken) {
        isBroken = broken;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public List<Trip> getTrips() {
        return trips;
    }

    public void setTrips(List<Trip> trips) {
        this.trips = trips;
    }
}
