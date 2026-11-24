package com.diary.service;

import com.diary.domain.DiaryEntry;
import com.diary.repository.EntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class EntryServiceTest {

    private static final long USER_ID = 1L;
    private static final long OTHER_USER_ID = 2L;
    private static final long GENERATED_ID = 10L;
    private static final long UNKNOWN_ID = 999_999L;

    private static final String TITLE = "День у Львові";
    private static final String CONTENT = "Гуляв містом.";

    private static final Instant NOW = Instant.parse("2026-11-17T10:00:00Z");
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 17);
    private static final LocalDateTime NOW_DATE_TIME = LocalDateTime.of(2026, 11, 17, 10, 0);

    @Mock
    private EntryRepository repository;

    private EntryService service;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(NOW, ZoneOffset.UTC);
        service = new EntryService(repository, fixedClock);
    }

    private DiaryEntry savedEntry(long id) {
        return new DiaryEntry.Builder()
                .id(id)
                .userId(USER_ID)
                .title(TITLE)
                .content(CONTENT)
                .date(TODAY)
                .createdAt(NOW_DATE_TIME)
                .build();
    }

    @Test
    void createEntry_blankTitle_throwsAndDoesNotCallRepository() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createEntry(USER_ID, "  ", CONTENT));

        verifyNoInteractions(repository);
    }

    @Test
    void createEntry_blankContent_throwsAndDoesNotCallRepository() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createEntry(USER_ID, TITLE, "  "));

        verifyNoInteractions(repository);
    }

// --- findById ---

    @Test
    void findById_entryExists_returnsEntryFromRepository() {
        DiaryEntry entry = savedEntry(GENERATED_ID);
        when(repository.findById(GENERATED_ID, USER_ID))
                .thenReturn(Optional.of(entry));

        Optional<DiaryEntry> result = service.findById(GENERATED_ID, USER_ID);

        assertEquals(Optional.of(entry), result);
    }

    @Test
    void findById_unknownId_returnsEmpty() {
        when(repository.findById(UNKNOWN_ID, USER_ID))
                .thenReturn(Optional.empty());

        Optional<DiaryEntry> result = service.findById(UNKNOWN_ID, USER_ID);

        assertEquals(Optional.empty(), result);
        verify(repository).findById(UNKNOWN_ID, USER_ID);
    }

    @Test
    void findById_passesUserIdToRepository() {
        service.findById(GENERATED_ID, OTHER_USER_ID);

        verify(repository).findById(GENERATED_ID, OTHER_USER_ID);
    }

// --- findAllByUserId ---

    @Test
    void findAllByUserId_returnsEntriesFromRepository() {
        DiaryEntry first = savedEntry(10L);
        DiaryEntry second = savedEntry(11L);
        List<DiaryEntry> expected = List.of(second, first);
        when(repository.findAllByUserId(USER_ID)).thenReturn(expected);

        List<DiaryEntry> result = service.findAllByUserId(USER_ID);

        assertEquals(expected, result);
    }

    @Test
    void findAllByUserId_passesUserIdToRepository() {
        service.findAllByUserId(OTHER_USER_ID);

        verify(repository).findAllByUserId(OTHER_USER_ID);
    }
}