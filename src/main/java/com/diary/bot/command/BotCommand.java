package com.diary.bot.command;

import com.diary.bot.IncomingMessage;

/**
 * Визначає спільний контракт команд бота та представляє патерн Command.
 *
 * <p>{@link #name()} повертає назву команди разом зі слешем, наприклад
 * {@code /list}. За цією назвою маршрутизатор вибирає обробник.
 * {@link #execute(IncomingMessage)} виконує дію для отриманого повідомлення.
 */
public interface BotCommand {

    /**
     * Повертає назву, за якою маршрутизатор знаходить команду.
     *
     * @return назва команди зі знаком {@code /}, наприклад {@code /list}
     */
    String name();

    /**
     * Виконує дію команди для отриманого повідомлення.
     *
     * @param message повідомлення з ідентифікаторами користувача й чату та текстом команди
     */
    void execute(IncomingMessage message);
}