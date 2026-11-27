package com.diary.bot.command;

import com.diary.bot.IncomingMessage;
import com.diary.bot.MessageGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StartCommandTest {

    private static final long USER_ID = 1L;
    private static final long CHAT_ID = 100L;
    private static final String WELCOME_MESSAGE = """
            Привіт! Я щоденник.
            /new Заголовок
            Текст запису - створити запис
            /list - показати записи""";

    @Mock
    private MessageGateway gateway;

    private StartCommand command;

    @BeforeEach
    void setUp() {
        command = new StartCommand(gateway);
    }

    @Test
    void name_isStart() {
        assertEquals("/start", command.name());
    }

    @Test
    void execute_sendsWelcomeMessageToTheSameChat() {
        command.execute(new IncomingMessage(USER_ID, CHAT_ID, "/start"));

        verify(gateway).sendText(CHAT_ID, WELCOME_MESSAGE);
    }
}