package com.diary.bot;

import java.nio.file.Path;

/**
 * Визначає операції надсилання тексту й файлів до вказаного чату.
 *
 * <p>Команди використовують цей інтерфейс для відповіді користувачу.
 * Реалізація {@link TelegramMessageGateway} доставляє повідомлення через
 * Telegram, а в тестах її можна замінити тестовим об'єктом і перевірити
 * текст відповіді та ідентифікатор чату.
 */
public interface MessageGateway {
    /**
     * Надсилає текст до вказаного чату.
     *
     * @param chatId ідентифікатор чату призначення
     * @param text текст повідомлення
     */
    void sendText(long chatId, String text);

    /**
     * Надсилає локальний файл до вказаного чату як документ.
     *
     * @param chatId ідентифікатор чату призначення
     * @param file шлях до файлу, підготовленого для надсилання
     */
    void sendFile(long chatId, Path file);
}