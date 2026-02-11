package com.example.autodepot.repository.impl;

import com.example.autodepot.entity.Car;
import com.example.autodepot.repository.CarRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class CarRepositoryImpl implements CarRepository {

    private final JdbcTemplate jdbcTemplate;

    public CarRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Car> findByIsBrokenFalse() {
        return jdbcTemplate.query("SELECT id, capacity, is_broken, version FROM cars WHERE is_broken = false",
            (rs, rowNum) -> mapCar(rs));
    }

    @Override
    public Optional<Car> findById(Long id) {
        List<Car> list = jdbcTemplate.query("SELECT id, capacity, is_broken, version FROM cars WHERE id = ?",
            (rs, rowNum) -> mapCar(rs), id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public long count() {
        Long cnt = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM cars", Long.class);
        return cnt != null ? cnt : 0;
    }

    @Override
    public Car save(Car car) {
        if (car.getId() == null) {
            var keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(con -> {
                var ps = con.prepareStatement(
                    "INSERT INTO cars (capacity, is_broken, version) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
                ps.setDouble(1, car.getCapacity());
                ps.setBoolean(2, car.isBroken());
                ps.setObject(3, car.getVersion());
                return ps;
            }, keyHolder);
            Number id = (Number) keyHolder.getKeys().get("id");
            car.setId(id != null ? id.longValue() : 0L);
        } else {
            jdbcTemplate.update(
                "UPDATE cars SET capacity = ?, is_broken = ?, version = ? WHERE id = ?",
                car.getCapacity(), car.isBroken(), car.getVersion(), car.getId());
        }
        return car;
    }

    @Override
    public void deleteAll() {
        jdbcTemplate.update("DELETE FROM cars");
    }

    private static Car mapCar(java.sql.ResultSet rs) throws java.sql.SQLException {
        var c = new Car();
        c.setId(rs.getLong("id"));
        c.setCapacity(rs.getDouble("capacity"));
        c.setBroken(rs.getBoolean("is_broken"));
        c.setVersion(rs.getObject("version") != null ? rs.getInt("version") : null);
        return c;
    }
}
