package com.diary.bot;

import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Адаптер між {@link MessageGateway} і клієнтом Telegram.
 *
 * <p>Текст надсилає через {@code SendMessage}. Якщо він довший за 4096 одиниць
 * UTF-16, ділить його на частини й надсилає їх послідовно. Для поділу
 * використовує останній доступний перенос рядка в межах ліміту.
 * Якщо такого переносу немає, розрізає текст на межі ліміту.
 *
 * <p>Файл із переданого шляху надсилає як документ через {@code SendDocument}.
 * Помилку Telegram API обгортає в {@link IllegalStateException},
 * зберігаючи початковий виняток як причину.
 */
public class TelegramMessageGateway implements MessageGateway {

    static final int MAX_MESSAGE_LENGTH = 4096;   // ліміт Telegram
    private static final char LINE_BREAK = '\n';

    private final TelegramClient client;
    /**
     * Створює адаптер для надсилання повідомлень через Telegram.
     *
     * @param client клієнт Telegram, який виконує запити від імені бота
     */
    public TelegramMessageGateway(TelegramClient client) {
        this.client = client;
    }

    /**
     * Надсилає текст у чат, послідовно відправляючи частини завдовжки
     * не більше {@value #MAX_MESSAGE_LENGTH} одиниць UTF-16.
     * Для порожнього рядка запит до Telegram не виконується.
     *
     * @param chatId ідентифікатор чату призначення
     * @param text повний текст повідомлення
     * @throws IllegalStateException якщо Telegram API повідомляє про помилку
     *         під час надсилання однієї з частин
     */
    @Override
    public void sendText(long chatId, String text) {
        for (String chunk : splitIntoChunks(text, MAX_MESSAGE_LENGTH)) {
            sendChunk(chatId, chunk);
        }
    }

    /**
     * Передає локальний файл до Telegram як документ.
     *
     * @param chatId ідентифікатор чату призначення
     * @param file шлях до файлу для надсилання
     * @throws IllegalStateException якщо Telegram API повідомляє про помилку надсилання
     */
    @Override
    public void sendFile(long chatId, Path file) {
        try {
            client.execute(SendDocument.builder()
                    .chatId(chatId)
                    .document(new InputFile(file.toFile()))
                    .build());
        } catch (TelegramApiException e) {
            throw new IllegalStateException("Failed to send file to chat " + chatId, e);
        }
    }

    /**
     * Надсилає одну підготовлену частину тексту окремим запитом до Telegram.
     *
     * @param chatId ідентифікатор чату призначення
     * @param chunk частина тексту в межах допустимої довжини
     * @throws IllegalStateException якщо Telegram API повідомляє про помилку надсилання
     */
    private void sendChunk(long chatId, String chunk) {
        try {
            client.execute(SendMessage.builder().chatId(chatId).text(chunk).build());
        } catch (TelegramApiException e) {
            throw new IllegalStateException("Failed to send message to chat " + chatId, e);
        }
    }

    /**
     * Ділить текст на частини в початковому порядку. За можливості використовує
     * останній перенос рядка в межах ліміту й відкидає перенос, на якому
     * відбувся поділ. Інакше розрізає текст на межі максимальної довжини.
     *
     * @param text текст для поділу
     * @param maxLength максимальна довжина однієї частини в одиницях UTF-16
     * @return частини тексту або порожній список для порожнього рядка
     * @throws IllegalArgumentException якщо {@code maxLength <= 0}
     */
    static List<String> splitIntoChunks(String text, int maxLength) {
        if (maxLength <= 0) {
            throw new IllegalArgumentException("maxLength must be positive");
        }
        List<String> chunks = new ArrayList<>();
        String remaining = text;
        while (remaining.length() > maxLength) {
            int lineBreak = remaining.lastIndexOf(LINE_BREAK, maxLength);
            if (lineBreak > 0) {
                chunks.add(remaining.substring(0, lineBreak));
                remaining = remaining.substring(lineBreak + 1);
            } else {
                chunks.add(remaining.substring(0, maxLength));
                remaining = remaining.substring(maxLength);
            }
        }
        if (!remaining.isEmpty()) {
            chunks.add(remaining);
        }
        return chunks;
    }
}