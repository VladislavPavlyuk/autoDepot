package com.example.autodepot.repository;

import com.example.autodepot.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    
    @Query("SELECT t.driver.name, COUNT(t), SUM(t.order.weight) " +
           "FROM Trip t " +
           "GROUP BY t.driver.id, t.driver.name")
    List<Object[]> findStatsByDriver();
    
    boolean existsByOrderId(Long orderId);
}
