package com.diary.bot;

import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

/**
 * Приймає оновлення Telegram і передає текстові повідомлення з приватних чатів
 * до {@link CommandRouter}.
 *
 * <p>З повідомлення бере ідентифікатор відправника, ідентифікатор чату й текст
 * та створює {@link IncomingMessage}. Оновлення з груп і повідомлення без тексту
 * пропускає до виклику маршрутизатора. Завдяки цьому команда {@code /list}
 * не може вивести особисті записи користувача в груповий чат.
 */
public class DiaryBot implements LongPollingSingleThreadUpdateConsumer {

    private final CommandRouter router;

    /**
     * Створює обробник оновлень Telegram.
     *
     * @param router маршрутизатор, якому передаються команди з приватних чатів
     */
    public DiaryBot(CommandRouter router) {
        this.router = router;
    }

    /**
     * Передає текстове повідомлення з приватного чату до маршрутизатора.
     * Пропускає оновлення без текстового повідомлення, відправника або чату,
     * а також повідомлення з груп.
     *
     * @param update оновлення, отримане від Telegram
     */
    @Override
    public void consume(Update update) {
        Message message = update.getMessage();

        if (message == null || !message.hasText() || message.getFrom() == null
                || message.getChat() == null || !message.getChat().isUserChat()) {
            return;
        }

        IncomingMessage incoming = new IncomingMessage(
                message.getFrom().getId(),
                message.getChatId(),
                message.getText()
        );

        router.route(incoming);
    }
}