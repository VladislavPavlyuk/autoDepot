package com.example.autodepot.service;

import java.util.List;

public interface ActivityLogService {

    List<String> readRecentActivity(int limit);
}
