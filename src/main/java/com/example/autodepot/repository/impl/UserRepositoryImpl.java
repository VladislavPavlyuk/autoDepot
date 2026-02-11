package com.example.autodepot.repository.impl;

import com.example.autodepot.entity.Role;
import com.example.autodepot.entity.User;
import com.example.autodepot.repository.RoleRepository;
import com.example.autodepot.repository.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RoleRepository roleRepository;

    public UserRepositoryImpl(JdbcTemplate jdbcTemplate, RoleRepository roleRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.roleRepository = roleRepository;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        List<User> list = jdbcTemplate.query(
            "SELECT id, username, password FROM users WHERE username = ?",
            (rs, rowNum) -> mapUser(rs), username);
        if (list.isEmpty()) return Optional.empty();
        User u = list.get(0);
        u.setRoles(loadRoles(u.getId()));
        return Optional.of(u);
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            var keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(con -> {
                var ps = con.prepareStatement(
                    "INSERT INTO users (username, password) VALUES (?, ?)",
                    java.sql.Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getPassword());
                return ps;
            }, keyHolder);
            Number id = (Number) keyHolder.getKeys().get("id");
            user.setId(id != null ? id.longValue() : 0L);
        } else {
            jdbcTemplate.update("UPDATE users SET username = ?, password = ? WHERE id = ?",
                user.getUsername(), user.getPassword(), user.getId());
            jdbcTemplate.update("DELETE FROM user_roles WHERE user_id = ?", user.getId());
        }
        for (Role r : user.getRoles()) {
            jdbcTemplate.update("INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)",
                user.getId(), r.getId());
        }
        return user;
    }

    private User mapUser(java.sql.ResultSet rs) throws java.sql.SQLException {
        var u = new User();
        u.setId(rs.getLong("id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        return u;
    }

    private Set<Role> loadRoles(Long userId) {
        List<Long> roleIds = jdbcTemplate.query(
            "SELECT role_id FROM user_roles WHERE user_id = ?",
            (rs, rowNum) -> rs.getLong("role_id"), userId);
        Set<Role> roles = new HashSet<>();
        for (Long roleId : roleIds) {
            roleRepository.findById(roleId).ifPresent(roles::add);
        }
        return roles;
    }
}
