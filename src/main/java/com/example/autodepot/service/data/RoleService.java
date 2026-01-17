package com.example.autodepot.service.data;

import com.example.autodepot.entity.Role;

import java.util.Optional;

public interface RoleService {
    Optional<Role> findByName(String name);

    Role save(Role role);
}
