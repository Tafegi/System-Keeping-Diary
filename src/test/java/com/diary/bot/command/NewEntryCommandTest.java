package com.diary.bot.command;

import com.diary.bot.EntryFormatter;
import com.diary.bot.IncomingMessage;
import com.diary.bot.MessageGateway;
import com.diary.service.EntryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewEntryCommandTest {

    private static final long USER_ID = 1L;
    private static final long CHAT_ID = 100L;
    private static final String USAGE_MESSAGE = "Формат: /new Заголовок, з нового рядка текст запису";
    private static final String SAVED_MESSAGE = "Збережено";

    @Mock
    private EntryService service;
    @Mock
    private MessageGateway gateway;

    private NewEntryCommand command;

    @BeforeEach
    void setUp() {
        command = new NewEntryCommand(service, new EntryFormatter(), gateway);
    }

    private IncomingMessage message(String text) {
        return new IncomingMessage(USER_ID, CHAT_ID, text);
    }

    @Test
    void name_isNew() {
        assertEquals("/new", command.name());
    }

    @Test
    void execute_titleAndContent_createsEntryAndConfirms() {
        command.execute(message("/new Заголовок\nТекст"));

        verify(service).createEntry(USER_ID, "Заголовок", "Текст");
        verify(gateway).sendText(CHAT_ID, SAVED_MESSAGE);
    }

    @Test
    void execute_multilineContent_keepsAllLines() {
        command.execute(message("/new Заголовок\nРядок 1\nРядок 2"));

        verify(service).createEntry(USER_ID, "Заголовок", "Рядок 1\nРядок 2");
    }

    @Test
    void execute_extraSpaces_trimsTitleAndContent() {
        command.execute(message("/new   Заголовок  \n  Текст  "));

        verify(service).createEntry(USER_ID, "Заголовок", "Текст");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/new",
            "/new   ",
            "/new Заголовок",
            "/new Заголовок\n   ",
            "/new \nТекст"
    })
    void execute_titleOrContentMissing_showsUsageAndSavesNothing(String text) {
        command.execute(message(text));

        verifyNoInteractions(service);
        verify(gateway).sendText(CHAT_ID, USAGE_MESSAGE);
    }

    @Test
    void execute_serviceRejectsData_exceptionGoesToRouterAndNothingIsSent() {
        when(service.createEntry(USER_ID, "Заголовок", "Текст"))
                .thenThrow(new IllegalArgumentException("invalid"));
        IncomingMessage message = message("/new Заголовок\nТекст");

        assertThrows(IllegalArgumentException.class, () -> command.execute(message));

        verifyNoInteractions(gateway);
    }
}