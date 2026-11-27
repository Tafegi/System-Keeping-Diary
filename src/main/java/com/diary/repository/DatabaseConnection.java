package com.diary.repository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static final String DEFAULT_CONFIG = "src/main/resources/database.properties";
    private static final String URL_KEY = "db.url";
    private static final String USERNAME_KEY = "db.username";
    private static final String PASSWORD_KEY = "db.password";

    private final String url;
    private final String username;
    private final String password;

    public DatabaseConnection(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public static DatabaseConnection fromProperties() {
        Path config = Path.of(System.getProperty("diary.db.config", DEFAULT_CONFIG));
        Properties props = new Properties();

        try (InputStream input = Files.newInputStream(config)) {
            props.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + config.toAbsolutePath(), e);
        }

        return new DatabaseConnection(
                props.getProperty(URL_KEY),
                props.getProperty(USERNAME_KEY),
                props.getProperty(PASSWORD_KEY)
        );
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}