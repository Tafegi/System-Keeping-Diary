package com.diary.bot.command;

import com.diary.bot.EntryFormatter;
import com.diary.bot.IncomingMessage;
import com.diary.bot.MessageGateway;
import com.diary.service.EntryService;

/**
 * Створює запис щоденника за командою {@code /new}.
 *
 * <p>Перший рядок аргументів містить заголовок, наступні рядки — текст запису.
 * Команда прибирає пробіли по краях обох частин і зберігає переноси рядків
 * усередині тексту. Якщо заголовок і текст не розділено переносом рядка,
 * надсилає підказку з форматом введення.
 *
 * <p>Передає ідентифікатор відправника, заголовок і текст до
 * {@link EntryService#createEntry(long, String, String)}.
 * Після успішного повернення з сервісу надсилає підтвердження «Збережено».
 */
public class NewEntryCommand implements BotCommand {

    private static final String NAME = "/new";
    static final String USAGE_MESSAGE = "Формат: /new Заголовок, з нового рядка текст запису";
    static final String SAVED_MESSAGE = "Збережено";
    private static final char LINE_BREAK = '\n';

    private final EntryService service;
    private final EntryFormatter formatter;
    private final MessageGateway gateway;

    /**
     * Створює команду додавання запису до щоденника.
     *
     * @param service сервіс створення та збереження записів
     * @param formatter форматувальник записів, збережений у полі класу;
     *                  поточна реалізація команди його не використовує
     * @param gateway засіб надсилання підказки або підтвердження
     */
    public NewEntryCommand(EntryService service, EntryFormatter formatter, MessageGateway gateway) {
        this.service = service;
        this.formatter = formatter;
        this.gateway = gateway;
    }

    /** @return назва команди {@code /new} */
    @Override
    public String name() {
        return NAME;
    }

    /**
     * Розділяє аргументи за першим переносом рядка: перший рядок містить
     * заголовок, решта — текст запису. Прибирає пропуски по краях обох частин
     * і передає їх сервісу разом з ідентифікатором користувача.
     *
     * <p>Якщо розділювача немає, надсилає підказку з форматом команди.
     * Після успішного повернення з сервісу надсилає підтвердження «Збережено».
     *
     * @param message повідомлення з командою, заголовком і текстом запису
     * @throws IllegalArgumentException якщо сервіс відхиляє дані запису
     */
    @Override
    public void execute(IncomingMessage message) {
        String arguments = message.arguments();
        int lineBreak = arguments.indexOf(LINE_BREAK);
        if (lineBreak < 0) {
            gateway.sendText(message.chatId(), USAGE_MESSAGE);
            return;
        }
        String title = arguments.substring(0, lineBreak).strip();
        String content = arguments.substring(lineBreak + 1).strip();

        service.createEntry(message.userId(), title, content);
        gateway.sendText(message.chatId(), SAVED_MESSAGE);
    }
}