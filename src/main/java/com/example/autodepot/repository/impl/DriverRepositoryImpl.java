package com.example.autodepot.repository.impl;

import com.example.autodepot.entity.Driver;
import com.example.autodepot.repository.DriverRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class DriverRepositoryImpl implements DriverRepository {

    private final JdbcTemplate jdbcTemplate;

    public DriverRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Driver> findByIsAvailableTrue() {
        List<Driver> drivers = jdbcTemplate.query(
            "SELECT id, name, license_year, is_available, earnings FROM drivers WHERE is_available = true",
            (rs, rowNum) -> mapDriver(rs));
        for (Driver d : drivers) {
            d.setLicenseCategories(loadLicenseCategories(d.getId()));
        }
        return drivers;
    }

    @Override
    public Optional<Driver> findById(Long id) {
        List<Driver> list = jdbcTemplate.query(
            "SELECT id, name, license_year, is_available, earnings FROM drivers WHERE id = ?",
            (rs, rowNum) -> mapDriver(rs), id);
        if (list.isEmpty()) return Optional.empty();
        Driver d = list.get(0);
        d.setLicenseCategories(loadLicenseCategories(d.getId()));
        return Optional.of(d);
    }

    @Override
    public List<Driver> findAll() {
        List<Driver> drivers = jdbcTemplate.query(
            "SELECT id, name, license_year, is_available, earnings FROM drivers ORDER BY id",
            (rs, rowNum) -> mapDriver(rs));
        for (Driver d : drivers) {
            d.setLicenseCategories(loadLicenseCategories(d.getId()));
        }
        return drivers;
    }

    @Override
    public long count() {
        Long cnt = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM drivers", Long.class);
        return cnt != null ? cnt : 0;
    }

    @Override
    public Driver save(Driver driver) {
        if (driver.getId() == null) {
            var keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(con -> {
                var ps = con.prepareStatement(
                    "INSERT INTO drivers (name, license_year, is_available, earnings) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, driver.getName());
                ps.setInt(2, driver.getLicenseYear());
                ps.setBoolean(3, driver.isAvailable());
                ps.setDouble(4, driver.getEarnings());
                return ps;
            }, keyHolder);
            Number id = (Number) keyHolder.getKeys().get("id");
            driver.setId(id != null ? id.longValue() : 0L);
        } else {
            jdbcTemplate.update(
                "UPDATE drivers SET name = ?, license_year = ?, is_available = ?, earnings = ? WHERE id = ?",
                driver.getName(), driver.getLicenseYear(), driver.isAvailable(), driver.getEarnings(), driver.getId());
            jdbcTemplate.update("DELETE FROM driver_license_categories WHERE driver_id = ?", driver.getId());
        }
        for (String cat : driver.getLicenseCategories()) {
            jdbcTemplate.update("INSERT INTO driver_license_categories (driver_id, category) VALUES (?, ?)",
                driver.getId(), cat);
        }
        return driver;
    }

    @Override
    public void deleteAll() {
        jdbcTemplate.update("DELETE FROM driver_license_categories");
        jdbcTemplate.update("DELETE FROM drivers");
    }

    private Driver mapDriver(java.sql.ResultSet rs) throws java.sql.SQLException {
        var d = new Driver();
        d.setId(rs.getLong("id"));
        d.setName(rs.getString("name"));
        d.setLicenseYear(rs.getInt("license_year"));
        d.setAvailable(rs.getBoolean("is_available"));
        d.setEarnings(rs.getDouble("earnings"));
        return d;
    }

    private List<String> loadLicenseCategories(Long driverId) {
        List<String> cats = jdbcTemplate.query(
            "SELECT category FROM driver_license_categories WHERE driver_id = ? ORDER BY category",
            (rs, rowNum) -> rs.getString("category"), driverId);
        return cats != null ? cats : new ArrayList<>();
    }
}
