package com.diary.bot;

import com.diary.bot.command.BotCommand;
import com.diary.repository.RepositoryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommandRouterTest {

    private static final long USER_ID = 1L;
    private static final long CHAT_ID = 100L;
    private static final String NEW_NAME = "/new";
    private static final String LIST_NAME = "/list";
    static final String UNKNOWN_COMMAND_MESSAGE = "Невідома команда. Напишіть /start";
    static final String GENERIC_ERROR_MESSAGE = "Сталася помилка, спробуйте пізніше";

    @Mock
    private BotCommand newCommand;
    @Mock
    private BotCommand listCommand;
    @Mock
    private MessageGateway gateway;

    private CommandRouter router;

    @BeforeEach
    void setUp() {
        when(newCommand.name()).thenReturn(NEW_NAME);
        when(listCommand.name()).thenReturn(LIST_NAME);
        router = new CommandRouter(List.of(newCommand, listCommand), gateway);
    }

    private IncomingMessage message(String text) {
        return new IncomingMessage(USER_ID, CHAT_ID, text);
    }


    @Test
    void route_knownCommand_executesOnlyThatCommand() {
        IncomingMessage message = message("/list");

        router.route(message);

        verify(listCommand).execute(message);
        verify(newCommand, never()).execute(any());
    }

    @Test
    void route_commandWithBotName_executesCommand() {
        IncomingMessage message = message("/list@MyBot");

        router.route(message);

        verify(listCommand).execute(message);
    }

    @Test
    void route_unknownCommand_sendsHintAndExecutesNothing() {
        router.route(message("/foo"));

        verify(gateway).sendText(CHAT_ID, UNKNOWN_COMMAND_MESSAGE);
        verify(newCommand, never()).execute(any());
        verify(listCommand, never()).execute(any());
    }

    @Test
    void route_plainText_sendsHint() {
        router.route(message("привіт"));

        verify(gateway).sendText(CHAT_ID, UNKNOWN_COMMAND_MESSAGE);
    }


    @Test
    void route_commandRejectsInput_userSeesTheReason() {
        IncomingMessage message = message("/new щось");
        String reason = "Заголовок надто довгий";
        doThrow(new IllegalArgumentException(reason)).when(newCommand).execute(message);

        router.route(message);

        verify(gateway).sendText(CHAT_ID, reason);
    }

    @Test
    void route_repositoryFails_userSeesGenericMessageWithoutDetails() {
        IncomingMessage message = message("/list");
        doThrow(new RepositoryException("connection refused")).when(listCommand).execute(message);

        router.route(message);

        verify(gateway).sendText(CHAT_ID, GENERIC_ERROR_MESSAGE);
        verifyNoMoreInteractions(gateway);
    }

    @Test
    void route_unexpectedError_doesNotCrashAndSendsGenericMessage() {
        IncomingMessage message = message("/list");
        doThrow(new IllegalStateException("boom")).when(listCommand).execute(message);

        assertDoesNotThrow(() -> router.route(message));

        verify(gateway).sendText(CHAT_ID, GENERIC_ERROR_MESSAGE);
    }


    @Test
    void constructor_duplicateCommandNames_fails() {
        BotCommand first = mock(BotCommand.class);
        BotCommand second = mock(BotCommand.class);
        when(first.name()).thenReturn(NEW_NAME);
        when(second.name()).thenReturn(NEW_NAME);
        List<BotCommand> commands = List.of(first, second);

        assertThrows(IllegalStateException.class, () -> new CommandRouter(commands, gateway));
    }
}