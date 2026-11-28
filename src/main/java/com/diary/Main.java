package com.diary;

import com.diary.bot.CommandRouter;
import com.diary.bot.DiaryBot;
import com.diary.bot.EntryFormatter;
import com.diary.bot.MessageGateway;
import com.diary.bot.TelegramMessageGateway;
import com.diary.bot.command.ListCommand;
import com.diary.bot.command.NewEntryCommand;
import com.diary.bot.command.StartCommand;
import com.diary.repository.DatabaseConnection;
import com.diary.repository.EntryRepository;
import com.diary.repository.JdbcEntryRepository;
import com.diary.service.EntryService;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.Clock;
import java.util.List;
import java.util.logging.Logger;

/**
 * Точка входу Telegram-бота для ведення щоденника.
 * Створює репозиторій, сервіс і команди та з'єднує їх із Telegram.
 */
public class Main {

    private static final String TOKEN_ENV_VARIABLE = "BOT_TOKEN";
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    /**
     * Реєструє бота й очікує оновлень від Telegram у режимі long polling.
     *
     * @param args аргументи командного рядка, які застосунок не використовує
     * @throws Exception якщо не вдалося зареєструвати бота, очікування було
     *                   перервано або виникла помилка під час закриття застосунку
     */
    public static void main(String[] args) throws Exception {
        String token = readToken();
        TelegramClient client = new OkHttpTelegramClient(token);
        CommandRouter router = createRouter(new TelegramMessageGateway(client));

        try (TelegramBotsLongPollingApplication application = new TelegramBotsLongPollingApplication()) {
            application.registerBot(token, new DiaryBot(router));
            LOGGER.info("Bot started");
            Thread.currentThread().join();
        }
    }

    /**
     * Читає токен бота зі змінної середовища {@code BOT_TOKEN}.
     *
     * @return токен для доступу до Telegram Bot API
     * @throws IllegalStateException якщо змінна відсутня, порожня або містить лише пропуски
     */
    private static String readToken() {
        String token = System.getenv(TOKEN_ENV_VARIABLE);
        if (token == null || token.isBlank()) {
            throw new IllegalStateException(TOKEN_ENV_VARIABLE + " is not set");
        }
        return token;
    }

    /**
     * Створює сервіс щоденника та реєструє команди
     * {@code /start}, {@code /new} і {@code /list}.
     *
     * @param gateway засіб надсилання відповідей користувачу
     * @return маршрутизатор із зареєстрованими командами
     * @throws IllegalStateException якщо файл конфігурації бази даних не вдалося прочитати
     */
    private static CommandRouter createRouter(MessageGateway gateway) {
        EntryRepository repository = new JdbcEntryRepository(DatabaseConnection.fromProperties());
        EntryService service = new EntryService(repository, Clock.systemDefaultZone());
        EntryFormatter formatter = new EntryFormatter();

        return new CommandRouter(List.of(
                new StartCommand(gateway),
                new NewEntryCommand(service, formatter, gateway),
                new ListCommand(service, formatter, gateway)), gateway);
    }
}