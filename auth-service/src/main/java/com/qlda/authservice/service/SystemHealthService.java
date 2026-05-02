package com.qlda.authservice.service;

import com.qlda.authservice.dto.system.SystemHealthResponse;
import java.sql.Connection;
import java.time.LocalDateTime;
import javax.sql.DataSource;
import org.springframework.stereotype.Service;

@Service
public class SystemHealthService {

    private final DataSource dataSource;

    public SystemHealthService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public SystemHealthResponse getSystemHealth() {
        String databaseStatus = "DOWN";
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(2)) {
                databaseStatus = "UP";
            }
        } catch (Exception ignored) {
            databaseStatus = "DOWN";
        }
        String serviceStatus = "UP".equals(databaseStatus) ? "UP" : "DEGRADED";
        return new SystemHealthResponse(
                "auth-service",
                serviceStatus,
                databaseStatus,
                LocalDateTime.now()
        );
    }
}
