package com.example.autodepot.service.data;

import com.example.autodepot.entity.User;

import java.util.Optional;

public interface UserService {
    Optional<User> findByUsername(String username);

    User save(User user);
}
