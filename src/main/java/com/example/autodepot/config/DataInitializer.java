package com.example.autodepot.config;

import com.example.autodepot.entity.*;
import com.example.autodepot.service.data.CarService;
import com.example.autodepot.service.data.DriverService;
import com.example.autodepot.service.data.RoleService;
import com.example.autodepot.service.data.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UserService userService;
    private final RoleService roleService;
    private final DriverService driverService;
    private final CarService carService;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserService userService, RoleService roleService,
                           DriverService driverService, CarService carService,
                           PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.roleService = roleService;
        this.driverService = driverService;
        this.carService = carService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Role adminRole = roleService.findByName("ADMIN")
            .orElseGet(() -> {
                Role role = new Role("ADMIN");
                return roleService.save(role);
            });

        Role dispatcherRole = roleService.findByName("DISPATCHER")
            .orElseGet(() -> {
                Role role = new Role("DISPATCHER");
                return roleService.save(role);
            });

        if (userService.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            roles.add(dispatcherRole);
            admin.setRoles(roles);
            userService.save(admin);
        }

        if (driverService.count() == 0) {
            int currentYear = java.time.Year.now().getValue();
            Driver d1 = new Driver("Ivan Petrenko", currentYear - 12);
            d1.setLicenseCategories(List.of("B", "C"));
            driverService.save(d1);
            Driver d2 = new Driver("Mykhailo Kovalenko", currentYear - 8);
            d2.setLicenseCategories(List.of("B"));
            driverService.save(d2);
            Driver d3 = new Driver("Oleksandr Shevchenko", currentYear - 15);
            d3.setLicenseCategories(List.of("B", "D"));
            driverService.save(d3);
            Driver d4 = new Driver("Andrii Melnyk", currentYear - 3);
            d4.setLicenseCategories(List.of("B"));
            driverService.save(d4);
            Driver d5 = new Driver("Dmytro Bondarenko", currentYear - 6);
            d5.setLicenseCategories(List.of("B", "C"));
            driverService.save(d5);
        }

        if (carService.count() == 0) {
            carService.save(new Car(2000.0));
            carService.save(new Car(3500.0));
            carService.save(new Car(5000.0));
            carService.save(new Car(1500.0));
            carService.save(new Car(4000.0));
        }
    }
}
