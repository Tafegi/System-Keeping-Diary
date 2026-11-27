package com.diary.bot;

import com.diary.domain.DiaryEntry;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EntryFormatterTest {

    private static final long USER_ID = 1L;
    private static final LocalDate DATE = LocalDate.of(2026, 11, 17);
    private static final String FORMATTED_DATE = "17.11.2026";
    private static final String ELLIPSIS = "…";
    private static final String TITLE = "День у Львові";
    private static final int PREVIEW_LENGTH = 100;
    private static final String EMPTY_LIST_MESSAGE = "Записів ще немає";

    private final EntryFormatter formatter = new EntryFormatter();

    private DiaryEntry entry(long id, String title, String content) {
        return new DiaryEntry.Builder()
                .id(id)
                .userId(USER_ID)
                .title(title)
                .content(content)
                .date(DATE)
                .build();
    }

    private String expected(long id, String title, String preview) {
        return "№" + id + " · " + FORMATTED_DATE + "\n" + title + "\n" + preview;
    }


    @Test
    void format_shortContent_showsIdDateTitleAndFullContent() {
        DiaryEntry entry = entry(7, TITLE, "Гуляв містом.");

        assertEquals(expected(7, TITLE, "Гуляв містом."), formatter.format(entry));
    }

    @Test
    void format_contentOfPreviewLength_isNotTruncated() {
        String content = "a".repeat(PREVIEW_LENGTH);

        assertEquals(expected(1, TITLE, content), formatter.format(entry(1, TITLE, content)));
    }

    @Test
    void format_contentLongerThanPreview_isTruncatedWithEllipsis() {
        String content = "a".repeat(PREVIEW_LENGTH + 1);
        String preview = "a".repeat(PREVIEW_LENGTH) + ELLIPSIS;

        assertEquals(expected(1, TITLE, preview), formatter.format(entry(1, TITLE, content)));
    }

    @Test
    void format_contentWithLineBreaks_showsThemAsSpaces() {
        DiaryEntry entry = entry(1, TITLE, "Рядок 1\nРядок 2");

        assertEquals(expected(1, TITLE, "Рядок 1 Рядок 2"), formatter.format(entry));
    }


    @Test
    void formatList_emptyList_returnsEmptyListMessage() {
        assertEquals(EMPTY_LIST_MESSAGE, formatter.formatList(List.of()));
    }

    @Test
    void formatList_oneEntry_equalsFormatOfThatEntry() {
        DiaryEntry entry = entry(1, TITLE, "Текст");

        assertEquals(formatter.format(entry), formatter.formatList(List.of(entry)));
    }

    @Test
    void formatList_severalEntries_separatesThemWithBlankLine() {
        DiaryEntry first = entry(1, "Перший", "Текст 1");
        DiaryEntry second = entry(2, "Другий", "Текст 2");

        String expected = formatter.format(first) + "\n\n" + formatter.format(second);

        assertEquals(expected, formatter.formatList(List.of(first, second)));
    }
}