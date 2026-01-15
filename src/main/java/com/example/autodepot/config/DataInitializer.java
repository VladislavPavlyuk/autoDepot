package com.example.autodepot.config;

import com.example.autodepot.entity.*;
import com.example.autodepot.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DriverRepository driverRepository;
    private final CarRepository carRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, RoleRepository roleRepository,
                           DriverRepository driverRepository, CarRepository carRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.driverRepository = driverRepository;
        this.carRepository = carRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Role adminRole = roleRepository.findByName("ADMIN")
            .orElseGet(() -> {
                Role role = new Role("ADMIN");
                return roleRepository.save(role);
            });

        Role dispatcherRole = roleRepository.findByName("DISPATCHER")
            .orElseGet(() -> {
                Role role = new Role("DISPATCHER");
                return roleRepository.save(role);
            });

        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            roles.add(dispatcherRole);
            admin.setRoles(roles);
            userRepository.save(admin);
        }

        if (driverRepository.count() == 0) {
            driverRepository.save(new Driver("John Smith", 12));
            driverRepository.save(new Driver("Michael Johnson", 8));
            driverRepository.save(new Driver("David Williams", 15));
            driverRepository.save(new Driver("Robert Brown", 3));
            driverRepository.save(new Driver("James Davis", 6));
        }

        if (carRepository.count() == 0) {
            carRepository.save(new Car(2000.0));
            carRepository.save(new Car(3500.0));
            carRepository.save(new Car(5000.0));
            carRepository.save(new Car(1500.0));
            carRepository.save(new Car(4000.0));
        }
    }
}
