package com.example.autodepot.repository.impl;

import com.example.autodepot.entity.Role;
import com.example.autodepot.repository.RoleRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class RoleRepositoryImpl implements RoleRepository {

    private final JdbcTemplate jdbcTemplate;

    public RoleRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Role> findByName(String name) {
        List<Role> list = jdbcTemplate.query(
            "SELECT id, name FROM roles WHERE name = ?",
            (rs, rowNum) -> mapRole(rs), name);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public Optional<Role> findById(Long id) {
        List<Role> list = jdbcTemplate.query(
            "SELECT id, name FROM roles WHERE id = ?",
            (rs, rowNum) -> mapRole(rs), id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public Role save(Role role) {
        if (role.getId() == null) {
            var keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(con -> {
                var ps = con.prepareStatement(
                    "INSERT INTO roles (name) VALUES (?)",
                    java.sql.Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, role.getName());
                return ps;
            }, keyHolder);
            Number id = (Number) keyHolder.getKeys().get("id");
            role.setId(id != null ? id.longValue() : 0L);
        } else {
            jdbcTemplate.update("UPDATE roles SET name = ? WHERE id = ?", role.getName(), role.getId());
        }
        return role;
    }

    private static Role mapRole(java.sql.ResultSet rs) throws java.sql.SQLException {
        var r = new Role();
        r.setId(rs.getLong("id"));
        r.setName(rs.getString("name"));
        return r;
    }
}
