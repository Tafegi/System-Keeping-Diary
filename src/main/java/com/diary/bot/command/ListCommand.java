package com.diary.bot.command;

import com.diary.bot.EntryFormatter;
import com.diary.bot.IncomingMessage;
import com.diary.bot.MessageGateway;
import com.diary.domain.DiaryEntry;
import com.diary.service.EntryService;

import java.util.List;

/**
 * Обробляє команду {@code /list} і показує до п'яти останніх записів користувача.
 *
 * <p>Запитує записи за ідентифікатором відправника та бере перші п'ять
 * у порядку, отриманому від сервісу. Передає їх до {@link EntryFormatter}
 * і надсилає відформатований текст у чат. Якщо записів ще немає,
 * користувач отримує повідомлення про порожній щоденник.
 */
public class ListCommand implements BotCommand {

    private static final String NAME = "/list";
    static final int LIST_LIMIT = 5;

    private final EntryService service;
    private final EntryFormatter formatter;
    private final MessageGateway gateway;

    /**
     * Створює команду перегляду записів щоденника.
     *
     * @param service сервіс отримання записів користувача
     * @param formatter форматувальник списку для показу в чаті
     * @param gateway засіб надсилання готового списку
     */
    public ListCommand(EntryService service, EntryFormatter formatter, MessageGateway gateway) {
        this.service = service;
        this.formatter = formatter;
        this.gateway = gateway;
    }

    /** @return назва команди {@code /list} */
    @Override
    public String name() {
        return NAME;
    }

    /**
     * Отримує записи за ідентифікатором відправника та вибирає перші
     * {@value #LIST_LIMIT} у порядку, повернутому сервісом. Форматує їх
     * і надсилає в чат. Для порожнього списку надсилає «Записів ще немає».
     *
     * @param message повідомлення, що визначає власника записів і чат для відповіді
     */
    @Override
    public void execute(IncomingMessage message) {
        List<DiaryEntry> entries = service.findAllByUserId(message.userId());
        List<DiaryEntry> shown = entries.stream().limit(LIST_LIMIT).toList();

        gateway.sendText(message.chatId(), formatter.formatList(shown));
    }
}