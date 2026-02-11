package com.example.autodepot.repository.impl;

import com.example.autodepot.entity.Car;
import com.example.autodepot.entity.Driver;
import com.example.autodepot.entity.Order;
import com.example.autodepot.entity.Trip;
import com.example.autodepot.repository.TripRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class TripRepositoryImpl implements TripRepository {

    private final JdbcTemplate jdbcTemplate;

    public TripRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String TRIP_JOIN = """
        SELECT t.id, t.order_id, t.driver_id, t.car_id, t.start_time, t.end_time, t.status, t.payment, t.car_status_after_trip,
               o.id oid, o.destination, o.cargo_type, o.weight, o.created_at,
               d.id did, d.name, d.license_year, d.is_available, d.earnings,
               c.id cid, c.capacity, c.is_broken, c.version
        FROM trips t
        JOIN orders o ON t.order_id = o.id
        JOIN drivers d ON t.driver_id = d.id
        JOIN cars c ON t.car_id = c.id
        """;

    @Override
    public Optional<Trip> findById(Long id) {
        List<Trip> list = jdbcTemplate.query(TRIP_JOIN + " WHERE t.id = ?",
            (rs, rowNum) -> mapTrip(rs), id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<Trip> findAll() {
        return jdbcTemplate.query(TRIP_JOIN + " ORDER BY t.id", (rs, rowNum) -> mapTrip(rs));
    }

    @Override
    public Trip save(Trip trip) {
        long orderId = trip.getOrder().getId();
        long driverId = trip.getDriver().getId();
        long carId = trip.getCar().getId();
        LocalDateTime startTime = trip.getStartTime() != null ? trip.getStartTime() : LocalDateTime.now();

        if (trip.getId() == null) {
            var keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(con -> {
                var ps = con.prepareStatement(
                    "INSERT INTO trips (order_id, driver_id, car_id, start_time, end_time, status, payment, car_status_after_trip) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    java.sql.Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, orderId);
                ps.setLong(2, driverId);
                ps.setLong(3, carId);
                ps.setTimestamp(4, Timestamp.valueOf(startTime));
                ps.setObject(5, trip.getEndTime() != null ? Timestamp.valueOf(trip.getEndTime()) : null);
                ps.setString(6, trip.getStatus().name());
                ps.setObject(7, trip.getPayment());
                ps.setString(8, trip.getCarStatusAfterTrip());
                return ps;
            }, keyHolder);
            Number id = (Number) keyHolder.getKeys().get("id");
            trip.setId(id != null ? id.longValue() : 0L);
        } else {
            jdbcTemplate.update(
                "UPDATE trips SET order_id = ?, driver_id = ?, car_id = ?, start_time = ?, end_time = ?, status = ?, payment = ?, car_status_after_trip = ? WHERE id = ?",
                orderId, driverId, carId, Timestamp.valueOf(startTime),
                trip.getEndTime() != null ? Timestamp.valueOf(trip.getEndTime()) : null,
                trip.getStatus().name(), trip.getPayment(), trip.getCarStatusAfterTrip(), trip.getId());
        }
        return trip;
    }

    @Override
    public boolean existsByOrderId(Long orderId) {
        Long cnt = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM trips WHERE order_id = ?", Long.class, orderId);
        return cnt != null && cnt > 0;
    }

    @Override
    public List<Object[]> findStatsByDriver() {
        return jdbcTemplate.query(
            "SELECT d.name, COUNT(t.id), COALESCE(SUM(o.weight), 0) " +
            "FROM drivers d LEFT JOIN trips t ON d.id = t.driver_id AND t.status = 'COMPLETED' " +
            "LEFT JOIN orders o ON t.order_id = o.id " +
            "GROUP BY d.id, d.name",
            (rs, rowNum) -> new Object[]{rs.getString(1), rs.getLong(2), rs.getDouble(3)});
    }

    @Override
    public void deleteAll() {
        jdbcTemplate.update("DELETE FROM trips");
    }

    private static Trip mapTrip(java.sql.ResultSet rs) throws java.sql.SQLException {
        var trip = new Trip();
        trip.setId(rs.getLong("id"));
        trip.setStartTime(toLocalDateTime(rs.getTimestamp("start_time")));
        trip.setEndTime(toLocalDateTime(rs.getTimestamp("end_time")));
        trip.setStatus(Trip.TripStatus.valueOf(rs.getString("status")));
        trip.setPayment(rs.getObject("payment") != null ? rs.getDouble("payment") : null);
        trip.setCarStatusAfterTrip(rs.getString("car_status_after_trip"));

        var order = new Order();
        order.setId(rs.getLong("oid"));
        order.setDestination(rs.getString("destination"));
        order.setCargoType(rs.getString("cargo_type"));
        order.setWeight(rs.getDouble("weight"));
        order.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        trip.setOrder(order);

        var driver = new Driver();
        driver.setId(rs.getLong("did"));
        driver.setName(rs.getString("name"));
        driver.setLicenseYear(rs.getInt("license_year"));
        driver.setAvailable(rs.getBoolean("is_available"));
        driver.setEarnings(rs.getDouble("earnings"));
        trip.setDriver(driver);

        var car = new Car();
        car.setId(rs.getLong("cid"));
        car.setCapacity(rs.getDouble("capacity"));
        car.setBroken(rs.getBoolean("is_broken"));
        car.setVersion(rs.getObject("version") != null ? rs.getInt("version") : null);
        trip.setCar(car);

        return trip;
    }

    private static LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts != null ? ts.toLocalDateTime() : null;
    }
}
