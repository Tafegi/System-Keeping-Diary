package com.diary.repository;

/**
 * Повідомляє про збій під час виконання операції репозиторію.
 * Може містити початковий виняток для діагностики причини збою.
 */
public class RepositoryException extends RuntimeException {

    /**
     * Створює виняток з описом проблеми в репозиторії.
     *
     * @param message опис проблеми, наприклад відсутності згенерованого ключа
     */
    public RepositoryException(String message) {
        super(message);
    }

    /**
     * Створює виняток з описом операції та зберігає початкову причину збою.
     *
     * @param message опис операції, під час якої виникла помилка
     * @param cause початковий виняток
     */
    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}