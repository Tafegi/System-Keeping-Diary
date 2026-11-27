package com.diary.bot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IncomingMessageTest {

    private static final long USER_ID = 1L;
    private static final long CHAT_ID = 100L;

    private IncomingMessage message(String text) {
        return new IncomingMessage(USER_ID, CHAT_ID, text);
    }

    @Test
    void record_keepsAllFields() {
        IncomingMessage message = message("/list");

        assertEquals(USER_ID, message.userId());
        assertEquals(CHAT_ID, message.chatId());
        assertEquals("/list", message.text());
    }


    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\n", "привіт", "hello /list"})
    void command_notACommand_returnsEmptyString(String text) {
        assertEquals("", message(text).command());
    }

    @ParameterizedTest
    @CsvSource({
            "/list, /list",
            "'/new Заголовок', /new",
            "'/new@MyBot Заголовок', /new",
            "/list@MyBot, /list",
            "/LIST, /list"
    })
    void command_returnsLowercaseFirstWordWithoutBotName(String text, String expected) {
        assertEquals(expected, message(text).command());
    }

    @Test
    void command_lineBreakRightAfterCommand_actsAsSeparator() {
        assertEquals("/new", message("/new\nЗаголовок").command());
    }

    @Test
    void command_surroundingSpaces_areIgnored() {
        assertEquals("/list", message("  /list  ").command());
    }


    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "привіт", "/list", "/list   ", "/new@MyBot"})
    void arguments_noArguments_returnsEmptyString(String text) {
        assertEquals("", message(text).arguments());
    }

    @Test
    void arguments_returnsTextAfterCommandWithoutSurroundingSpaces() {
        assertEquals("Заголовок", message("/new   Заголовок  ").arguments());
    }

    @Test
    void arguments_keepsLineBreaksInside() {
        assertEquals("Заголовок\nТекст\nще рядок",
                message("/new Заголовок\nТекст\nще рядок").arguments());
    }

    @Test
    void arguments_commandWithBotName_skipsBotName() {
        assertEquals("Заголовок", message("/new@MyBot Заголовок").arguments());
    }

    @Test
    void arguments_lineBreakRightAfterCommand_isNotPartOfArguments() {
        assertEquals("Заголовок", message("/new\nЗаголовок").arguments());
    }
}