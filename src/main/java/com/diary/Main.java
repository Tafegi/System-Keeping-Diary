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
 * Точка входу. Лише створює об'єкти й з'єднує їх між собою, без жодної логіки.
 */
public class Main {

    private static final String TOKEN_ENV_VARIABLE = "BOT_TOKEN";
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

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

    private static String readToken() {
        String token = System.getenv(TOKEN_ENV_VARIABLE);
        if (token == null || token.isBlank()) {
            throw new IllegalStateException(TOKEN_ENV_VARIABLE + " is not set");
        }
        return token;
    }

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