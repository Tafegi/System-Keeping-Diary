package com.diary.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DiaryEntryTest {

    private static final long VALID_USER_ID = 42L;
    private static final long VALID_ID = 7L;
    private static final String VALID_TITLE = "День у Львові";
    private static final String VALID_CONTENT = "Гуляв містом.";
    private static final LocalDate VALID_DATE = LocalDate.of(2026, 11, 17);
    private static final LocalDateTime FIXED_CREATED_AT = LocalDateTime.of(2026, 11, 17, 10, 30);

    private DiaryEntry.Builder validBuilder() {
        return new DiaryEntry.Builder()
                .userId(VALID_USER_ID)
                .title(VALID_TITLE)
                .content(VALID_CONTENT)
                .date(VALID_DATE)
                .createdAt(FIXED_CREATED_AT);
    }

    @Test
    void build_withValidData_keepsAllFields() {
        DiaryEntry entry = validBuilder().id(VALID_ID).status(EntryStatus.ARCHIVED).build();

        assertEquals(VALID_ID, entry.getId());
        assertEquals(VALID_USER_ID, entry.getUserId());
        assertEquals(VALID_TITLE, entry.getTitle());
        assertEquals(VALID_CONTENT, entry.getContent());
        assertEquals(VALID_DATE, entry.getDate());
        assertEquals(EntryStatus.ARCHIVED, entry.getStatus());
        assertEquals(FIXED_CREATED_AT, entry.getCreatedAt());
    }

    /**
     *--- title ---
     */
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void build_withNullOrBlankTitle_throws(String title) {
        DiaryEntry.Builder builder = validBuilder().title(title);

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void build_withTitleOfMaxLength_succeeds() {
        String title = "a".repeat(DiaryEntry.MAX_TITLE_LENGTH);

        DiaryEntry entry = validBuilder().title(title).build();

        assertEquals(title, entry.getTitle());
    }

    @Test
    void build_withTitleOneCharLongerThanMax_throws() {
        String title = "a".repeat(DiaryEntry.MAX_TITLE_LENGTH + 1);
        DiaryEntry.Builder builder = validBuilder().title(title);

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    /**
     *--- content ---
     */
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void build_withNullOrBlankContent_throws(String content) {
        DiaryEntry.Builder builder = validBuilder().content(content);

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    /**
     *--- date ---
     */
    @Test
    void build_withNullDate_throws() {
        DiaryEntry.Builder builder = validBuilder().date(null);

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    /**
     *--- userId ---
     */
    @ParameterizedTest
    @ValueSource(longs = {0L, -1L})
    void build_withNonPositiveUserId_throws(long userId) {
        DiaryEntry.Builder builder = validBuilder().userId(userId);

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    /**
     *--- id ---
     */
    @Test
    void build_withoutId_usesNotSavedId() {
        DiaryEntry entry = validBuilder().build();

        assertEquals(DiaryEntry.NOT_SAVED_ID, entry.getId());
    }

    @Test
    void build_withNegativeId_throws() {
        DiaryEntry.Builder builder = validBuilder().id(-1L);

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    /**
     *--- status ---
     */
    @Test
    void build_withoutStatus_defaultsToSaved() {
        DiaryEntry entry = validBuilder().build();

        assertEquals(EntryStatus.SAVED, entry.getStatus());
    }

    @Test
    void build_withNullStatus_throws() {
        DiaryEntry.Builder builder = validBuilder().status(null);

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    /**
     *--- createdAt ---
     */
    @Test
    void build_withoutCreatedAt_setsCurrentTime() {
        DiaryEntry entry = new DiaryEntry.Builder()
                .userId(VALID_USER_ID)
                .title(VALID_TITLE)
                .content(VALID_CONTENT)
                .date(VALID_DATE)
                .build();

        assertNotNull(entry.getCreatedAt());
    }

    @Test
    void build_withNullCreatedAt_throws() {
        DiaryEntry.Builder builder = validBuilder().createdAt(null);

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    /**
     *--- equals / hashCode ---
     */
    @Test
    void equals_withSameFields_isEqualAndHasSameHashCode() {
        DiaryEntry first = validBuilder().build();
        DiaryEntry second = validBuilder().build();

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void equals_withDifferentTitle_isNotEqual() {
        DiaryEntry first = validBuilder().build();
        DiaryEntry second = validBuilder().title("Інший заголовок").build();

        assertNotEquals(first, second);
    }
}