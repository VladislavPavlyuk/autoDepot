package com.example.autodepot.service.logging;

import com.example.autodepot.entity.Trip;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class FileTripEventLogger implements TripEventLogger {
    private static final String LOG_FILE = "trips.log";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void logEvent(String event, Trip trip) {
        try {
            Path logPath = Paths.get(LOG_FILE);
            if (!Files.exists(logPath)) {
                Files.createFile(logPath);
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
                writer.printf("[%s] %s - Trip ID: %d, Driver: %s, Car ID: %d, Order: %s -> %s, Weight: %.2f kg, Status: %s%n",
                    LocalDateTime.now().format(FORMATTER),
                    event,
                    trip.getId(),
                    trip.getDriver().getName(),
                    trip.getCar().getId(),
                    trip.getOrder().getCargoType(),
                    trip.getOrder().getDestination(),
                    trip.getOrder().getWeight(),
                    trip.getStatus());

                if (trip.getPayment() != null) {
                    writer.printf("  Payment: %.2f, Car Status: %s%n",
                        trip.getPayment(), trip.getCarStatusAfterTrip());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }
}
