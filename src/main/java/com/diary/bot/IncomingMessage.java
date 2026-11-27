package com.diary.bot;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Містить дані вхідного повідомлення, потрібні для виконання команди щоденника.
 *
 * <p>{@link #command()} повертає команду в нижньому регістрі разом із початковим
 * слешем. Початкові пробіли та суфікс з іменем бота пропускаються.
 * Наприклад, для {@code "  /LIST@DiaryBot"} результатом буде {@code "/list"}.
 *
 * <p>{@link #arguments()} повертає текст після команди, прибираючи пробіли
 * й переноси рядків по краях. Переноси всередині тексту зберігаються,
 * тому команда {@code /new} може відокремити заголовок від вмісту запису.
 * Для {@code null} обидва методи повертають порожній рядок.
 *
 * @param userId ідентифікатор відправника, за яким визначається власник записів
 * @param chatId ідентифікатор чату, до якого потрібно надіслати відповідь
 * @param text початковий текст повідомлення
 */
public record IncomingMessage(long userId, long chatId, String text) {

    private static final Pattern COMMAND_PATTERN = Pattern.compile("^\\s*(/[^\\s@]+)");
    private static final Pattern COMMAND_PREFIX = Pattern.compile("^\\s*/[^\\s@]+(?:@\\S+)?\\s+");

    /**
     * Виділяє назву команди на початку повідомлення. Початкові пробіли
     * пропускаються, а назва приводиться до нижнього регістру.
     * Наприклад, {@code "  /LIST@DiaryBot"} дає {@code "/list"}.
     *
     * @return назва зі знаком {@code /} без імені бота або порожній рядок,
     *         якщо текст дорівнює {@code null} чи не починається з команди
     */
    public String command() {
        if (text == null) {
            return "";
        }

        Matcher matcher = COMMAND_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).toLowerCase(Locale.ROOT);
        }

        return "";
    }

    /**
     * Виділяє текст після команди та необов'язкового імені бота.
     * Прибирає пропуски й переноси рядків по краях аргументів,
     * зберігаючи переноси всередині тексту.
     *
     * @return аргументи команди або порожній рядок, якщо їх немає
     *         чи текст не містить команди на початку
     */
    public String arguments() {
        if (text == null) {
            return "";
        }

        Matcher matcher = COMMAND_PREFIX.matcher(text);
        if (matcher.find()) {
            return text.substring(matcher.end()).strip();
        }

        return "";
    }
}