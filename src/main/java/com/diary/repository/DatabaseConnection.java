package com.diary.repository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Зберігає параметри доступу до бази даних і відкриває JDBC-з'єднання.
 *
 * <p>Параметри можна передати через конструктор або прочитати з файлу
 * за допомогою {@link #fromProperties()}. Кожен виклик {@link #getConnection()}
 * відкриває окреме з'єднання, яке має закрити код, що його отримав.
 */
public class DatabaseConnection {

    private static final String DEFAULT_CONFIG = "src/main/resources/database.properties";
    private static final String URL_KEY = "db.url";
    private static final String USERNAME_KEY = "db.username";
    private static final String PASSWORD_KEY = "db.password";

    private final String url;
    private final String username;
    private final String password;

    /**
     * Зберігає параметри для подальшого відкриття з'єднань.
     *
     * @param url JDBC-адреса бази даних
     * @param username ім'я користувача бази даних
     * @param password пароль користувача бази даних
     */
    public DatabaseConnection(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    /**
     * Читає з файлу властивості {@code db.url}, {@code db.username}
     * та {@code db.password} і створює об'єкт із цими параметрами.
     *
     * <p>Шлях до файлу задається системною властивістю {@code diary.db.config}.
     * За замовчуванням використовується {@code src/main/resources/database.properties}
     * відносно поточної робочої директорії.
     *
     * @return об'єкт із прочитаними параметрами з'єднання
     * @throws IllegalStateException якщо файл не вдалося відкрити або прочитати
     */
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

    /**
     * Відкриває нове JDBC-з'єднання зі збереженими параметрами доступу.
     *
     * @return відкрите з'єднання, яке викликач має закрити після використання
     * @throws SQLException якщо JDBC-драйверу не вдалося встановити з'єднання
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}