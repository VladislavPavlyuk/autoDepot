package com.example.autodepot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String username;
    private String password;
    private Set<Role> roles = new HashSet<>();

    public User(String username, String password) {
        this(null, username, password, new HashSet<>());
    }
}
