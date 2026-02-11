package com.example.autodepot.repository;

import com.example.autodepot.entity.User;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findByUsername(String username);

    User save(User user);
}
