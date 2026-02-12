package com.example.autodepot.service.impl;

import com.example.autodepot.service.ActivityLogService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActivityLogServiceImpl implements ActivityLogService {

    private static final String LOG_FILE = "trips.log";

    @Override
    public List<String> readRecentActivity(int limit) {
        Path logPath = Paths.get(LOG_FILE);
        if (!Files.exists(logPath)) {
            return List.of();
        }
        try {
            List<String> lines = Files.readAllLines(logPath).stream()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .collect(Collectors.toList());
            int start = Math.max(0, lines.size() - limit);
            return lines.subList(start, lines.size());
        } catch (IOException e) {
            return List.of();
        }
    }
}
