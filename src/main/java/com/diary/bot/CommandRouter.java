package com.diary.bot;

import com.diary.bot.command.BotCommand;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Знаходить зареєстровану команду за її назвою та передає їй вхідне повідомлення.
 *
 * <p>Під час створення будує таблицю команд. Кожна команда повинна мати
 * унікальну назву. Якщо назву з повідомлення не знайдено, маршрутизатор
 * надсилає користувачу підказку звернутися до {@code /start}.
 *
 * <p>Якщо команда відхиляє введені дані через {@link IllegalArgumentException},
 * користувач отримує текст причини, якщо він є. Інші винятки типу
 * {@link RuntimeException}, що виникають під час виконання команди,
 * записуються в журнал, а користувачу надсилається загальне повідомлення.
 */
public class CommandRouter {

    static final String UNKNOWN_COMMAND_MESSAGE = "Невідома команда. Напишіть /start";
    static final String GENERIC_ERROR_MESSAGE = "Сталася помилка, спробуйте пізніше";
    private static final Logger LOGGER = Logger.getLogger(CommandRouter.class.getName());

    private final Map<String, BotCommand> commands;
    private final MessageGateway gateway;

    /**
     * Реєструє команди за назвами, які повертає {@link BotCommand#name()}.
     *
     * @param commands команди з унікальними назвами
     * @param gateway  засіб надсилання підказок і повідомлень про помилки
     * @throws IllegalStateException якщо дві команди мають однакову назву
     */
    public CommandRouter(List<BotCommand> commands, MessageGateway gateway) {
        this.commands = commands.stream()
                .collect(Collectors.toMap(BotCommand::name, Function.identity()));
        this.gateway = gateway;
    }

    /**
     * Знаходить і виконує команду з повідомлення. Якщо команда невідома
     * або повідомлення містить звичайний текст, надсилає підказку про {@code /start}.
     *
     * @param message повідомлення користувача
     */
    public void route(IncomingMessage message) {
        BotCommand command = commands.get(message.command());
        if (command == null) {
            gateway.sendText(message.chatId(), UNKNOWN_COMMAND_MESSAGE);
            return;
        }
        execute(command, message);
    }

    /**
     * Виконує знайдену команду. При помилці введення передає користувачу
     * причину відмови. Інші винятки типу {@link RuntimeException}, викинуті
     * командою, записує в журнал і надсилає загальне повідомлення про збій.
     *
     * @param command обробник, вибраний за назвою команди
     * @param message повідомлення, яке потрібно обробити
     */
    private void execute(BotCommand command, IncomingMessage message) {
        try {
            command.execute(message);
        } catch (IllegalArgumentException e) {
            reportInvalidInput(message, e);
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Command failed: " + message.command(), e);
            gateway.sendText(message.chatId(), GENERIC_ERROR_MESSAGE);
        }
    }

    /**
     * Надсилає в чат пояснення помилки введення. Якщо повідомлення винятку
     * дорівнює {@code null}, використовує загальний текст помилки.
     *
     * @param message повідомлення, з якого береться ідентифікатор чату
     * @param error   виняток із поясненням причини відмови
     */
    private void reportInvalidInput(IncomingMessage message, IllegalArgumentException error) {
        String reason = error.getMessage();
        gateway.sendText(message.chatId(), reason == null ? GENERIC_ERROR_MESSAGE : reason);
    }
}