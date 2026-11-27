package com.diary.bot.command;

import com.diary.bot.IncomingMessage;
import com.diary.bot.MessageGateway;

/**
 * Обробляє команду {@code /start}.
 *
 * <p>Надсилає в чат привітання та коротку інструкцію для двох дій щоденника:
 * створення запису через {@code /new} і перегляду записів через {@code /list}.
 */
public class StartCommand implements BotCommand {

    private static final String NAME = "/start";
    private static final String WELCOME_MESSAGE = """
            Привіт! Я щоденник.
            /new Заголовок
            Текст запису - створити запис
            /list - показати записи""";

    private final MessageGateway gateway;

    /**
     * Створює команду привітання користувача.
     *
     * @param gateway засіб надсилання привітання в чат
     */
    public StartCommand(MessageGateway gateway) {
        this.gateway = gateway;
    }

    /** @return назва команди {@code /start} */
    @Override
    public String name() {
        return NAME;
    }

    /**
     * Надсилає в чат привітання та підказку, як створити запис через
     * {@code /new} і переглянути записи через {@code /list}.
     *
     * @param message повідомлення, з якого береться ідентифікатор чату
     */
    @Override
    public void execute(IncomingMessage message) {
        gateway.sendText(message.chatId(), WELCOME_MESSAGE);
    }
}