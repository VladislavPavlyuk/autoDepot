package com.example.autodepot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    private Long id;
    private String name;
    private Set<User> users = new HashSet<>();

    public Role(String name) {
        this(null, name, new HashSet<>());
    }
}
