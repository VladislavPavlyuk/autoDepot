package com.example.autodepot.repository;

import com.example.autodepot.entity.Role;

import java.util.Optional;

public interface RoleRepository {

    Optional<Role> findByName(String name);

    Optional<Role> findById(Long id);

    Role save(Role role);
}
