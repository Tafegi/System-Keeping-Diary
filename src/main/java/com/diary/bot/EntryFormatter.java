package com.diary.bot;

import com.diary.domain.DiaryEntry;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Форматує записи щоденника для показу користувачу.
 *
 * <p>{@link #format(DiaryEntry)} повертає номер запису, дату у форматі
 * {@code dd.MM.uuuu}, заголовок і початок тексту. У попередньому перегляді
 * переноси рядків замінюються пробілами, а текст довший за 100 одиниць
 * UTF-16 обрізається та доповнюється знаком {@code …}.
 *
 * <p>{@link #formatList(List)} зберігає порядок отриманих записів
 * і розділяє їх порожнім рядком. Для порожнього списку повертає
 * повідомлення «Записів ще немає».
 */
public class EntryFormatter {

    private static final int PREVIEW_LENGTH = 100;   // скільки символів тексту показувати в списку
    private static final String EMPTY_LIST_MESSAGE = "Записів ще немає";

    /**
     * Форматує один запис для показу в чаті. Перший рядок містить номер і дату,
     * другий — заголовок, третій — попередній перегляд вмісту.
     *
     * @param entry запис щоденника
     * @return текст запису для надсилання користувачу
     */
    public String format(DiaryEntry entry) {
        return formatHeader(entry) + "\n"
                + entry.getTitle() + "\n"
                + buildPreview(entry.getContent());
    }

    /**
     * Формує перший рядок запису з його номера та дати.
     *
     * @param entry запис, з якого беруться ідентифікатор і дата
     * @return заголовний рядок, наприклад {@code №7 · 17.11.2026}
     */
    private String formatHeader(DiaryEntry entry) {
        String date = entry.getDate().format(DateTimeFormatter.ofPattern("dd.MM.uuuu"));
        return "№" + entry.getId() + " · " + date;
    }

    /**
     * Замінює переноси рядків пробілами. Якщо отриманий текст довший за
     * {@value #PREVIEW_LENGTH} одиниць UTF-16, обрізає його до цієї довжини
     * й додає знак {@code …}.
     *
     * @param content повний текст запису
     * @return однорядковий попередній перегляд
     */
    private String buildPreview(String content) {
        String preview = content.replaceAll("\\R", " ");
        if (preview.length() > PREVIEW_LENGTH) {
            return preview.substring(0, PREVIEW_LENGTH) + "…";
        }
        return preview;
    }

    /**
     * Форматує записи в порядку отримання та розділяє їх порожнім рядком.
     *
     * @param entries записи для показу
     * @return відформатований список або повідомлення «Записів ще немає»,
     *         якщо список порожній
     */
    public String formatList(List<DiaryEntry> entries) {
        if (entries.isEmpty()) {
            return EMPTY_LIST_MESSAGE;
        }
        return entries.stream()
                .map(this::format)
                .collect(Collectors.joining("\n\n"));
    }
}