package com.lottery.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseConfig {

    private static HikariDataSource dataSource;

    private DatabaseConfig() {
    }

    public static synchronized DataSource getDataSource() {
        if (dataSource == null) {
            dataSource = createDataSource();
            runMigrations(dataSource);
        }
        return dataSource;
    }

    private static HikariDataSource createDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(env("DB_URL", "jdbc:postgresql://localhost:5432/lottery"));
        config.setUsername(env("DB_USER", "lottery"));
        config.setPassword(env("DB_PASSWORD", "lottery"));
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(30_000);
        return new HikariDataSource(config);
    }

    private static void runMigrations(DataSource ds) {
        try (InputStream stream = DatabaseConfig.class.getResourceAsStream("/db/migration/V1__init.sql")) {
            if (stream == null) {
                throw new IllegalStateException("Migration script not found");
            }
            String sql = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            try (Connection connection = ds.getConnection();
                 Statement statement = connection.createStatement()) {
                statement.execute(sql);
            }
        } catch (IOException | SQLException e) {
            throw new IllegalStateException("Failed to run database migrations", e);
        }
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value != null && !value.isBlank() ? value : defaultValue;
    }
}
