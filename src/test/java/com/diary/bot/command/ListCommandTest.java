package com.diary.bot.command;

import com.diary.bot.EntryFormatter;
import com.diary.bot.IncomingMessage;
import com.diary.bot.MessageGateway;
import com.diary.domain.DiaryEntry;
import com.diary.service.EntryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListCommandTest {

    private static final long USER_ID = 1L;
    private static final long CHAT_ID = 100L;
    private static final LocalDate DATE = LocalDate.of(2026, 11, 17);
    private static final String EMPTY_LIST_MESSAGE = "Записів ще немає";
    static final int LIST_LIMIT = 5;

    @Mock
    private EntryService service;
    @Mock
    private MessageGateway gateway;

    private final EntryFormatter formatter = new EntryFormatter();
    private ListCommand command;

    @BeforeEach
    void setUp() {
        command = new ListCommand(service, formatter, gateway);
    }

    private IncomingMessage message() {
        return new IncomingMessage(USER_ID, CHAT_ID, "/list");
    }

    private DiaryEntry entry(long id) {
        return new DiaryEntry.Builder()
                .id(id)
                .userId(USER_ID)
                .title("Запис " + id)
                .content("Текст " + id)
                .date(DATE)
                .build();
    }

    private List<DiaryEntry> entries(int count) {
        List<DiaryEntry> entries = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            entries.add(entry(i));
        }
        return entries;
    }

    @Test
    void name_isList() {
        assertEquals("/list", command.name());
    }

    @Test
    void execute_userHasEntries_sendsFormattedList() {
        List<DiaryEntry> entries = entries(2);
        when(service.findAllByUserId(USER_ID)).thenReturn(entries);

        command.execute(message());

        verify(gateway).sendText(CHAT_ID, formatter.formatList(entries));
    }

    @Test
    void execute_noEntries_sendsEmptyListMessage() {
        when(service.findAllByUserId(USER_ID)).thenReturn(List.of());

        command.execute(message());

        verify(gateway).sendText(CHAT_ID, EMPTY_LIST_MESSAGE);
    }

    @Test
    void execute_exactlyLimitEntries_showsAll() {
        List<DiaryEntry> entries = entries(LIST_LIMIT);
        when(service.findAllByUserId(USER_ID)).thenReturn(entries);

        command.execute(message());

        verify(gateway).sendText(CHAT_ID, formatter.formatList(entries));
    }

    @Test
    void execute_moreThanLimitEntries_showsOnlyFirstOnes() {
        List<DiaryEntry> entries = entries(LIST_LIMIT + 2);
        when(service.findAllByUserId(USER_ID)).thenReturn(entries);

        command.execute(message());

        List<DiaryEntry> shown = entries.subList(0, LIST_LIMIT);
        verify(gateway).sendText(CHAT_ID, formatter.formatList(shown));
    }
}