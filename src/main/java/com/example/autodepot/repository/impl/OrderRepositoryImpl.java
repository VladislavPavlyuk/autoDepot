package com.example.autodepot.repository.impl;

import com.example.autodepot.entity.Order;
import com.example.autodepot.repository.OrderRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class OrderRepositoryImpl implements OrderRepository {

    private final JdbcTemplate jdbcTemplate;

    public OrderRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Order> findAll() {
        return jdbcTemplate.query(
            "SELECT id, destination, cargo_type, weight, created_at FROM orders ORDER BY id",
            (rs, rowNum) -> mapOrder(rs));
    }

    @Override
    public Optional<Order> findById(Long id) {
        List<Order> list = jdbcTemplate.query(
            "SELECT id, destination, cargo_type, weight, created_at FROM orders WHERE id = ?",
            (rs, rowNum) -> mapOrder(rs), id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public Order save(Order order) {
        if (order.getId() == null) {
            LocalDateTime createdAt = order.getCreatedAt() != null ? order.getCreatedAt() : LocalDateTime.now();
            var keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(con -> {
                var ps = con.prepareStatement(
                    "INSERT INTO orders (destination, cargo_type, weight, created_at) VALUES (?, ?, ?, ?)",
                    java.sql.Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, order.getDestination());
                ps.setString(2, order.getCargoType());
                ps.setDouble(3, order.getWeight());
                ps.setTimestamp(4, Timestamp.valueOf(createdAt));
                return ps;
            }, keyHolder);
            Number id = (Number) keyHolder.getKeys().get("id");
            order.setId(id != null ? id.longValue() : 0L);
            order.setCreatedAt(createdAt);
        } else {
            jdbcTemplate.update(
                "UPDATE orders SET destination = ?, cargo_type = ?, weight = ?, created_at = ? WHERE id = ?",
                order.getDestination(), order.getCargoType(), order.getWeight(),
                Timestamp.valueOf(order.getCreatedAt()), order.getId());
        }
        return order;
    }

    @Override
    public void deleteAll() {
        jdbcTemplate.update("DELETE FROM orders");
    }

    private static Order mapOrder(java.sql.ResultSet rs) throws java.sql.SQLException {
        var o = new Order();
        o.setId(rs.getLong("id"));
        o.setDestination(rs.getString("destination"));
        o.setCargoType(rs.getString("cargo_type"));
        o.setWeight(rs.getDouble("weight"));
        Timestamp ts = rs.getTimestamp("created_at");
        o.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);
        return o;
    }
}
