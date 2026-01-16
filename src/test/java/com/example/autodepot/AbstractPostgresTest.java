package com.example.autodepot;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@ActiveProfiles("test")
public abstract class AbstractPostgresTest {
    private static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse("postgres:16");
    private static final PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>(POSTGRES_IMAGE)
            .withDatabaseName("autodepot")
            .withUsername("autodepot")
            .withPassword("autodepot");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driverClassName", POSTGRES::getDriverClassName);
    }
}
