package com.diary.repository;

import com.diary.domain.DiaryEntry;
import com.diary.domain.EntryStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdbcEntryRepositoryTest {

    private static final String H2_URL = "jdbc:h2:mem:diary_test;DB_CLOSE_DELAY=-1";
    private static final String H2_USER = "sa";
    private static final String H2_PASSWORD = ""; 
    private static final String SCHEMA_RESOURCE = "/schema.sql";
    private static final String DROP_TABLE_SQL = "DROP TABLE IF EXISTS entry";

    private static final long USER_ID = 1L;
    private static final long OTHER_USER_ID = 2L;
    private static final long UNKNOWN_ID = 999_999L;

    private static final String TITLE = "День у Львові";
    private static final String CONTENT = "Гуляв містом.";
    private static final LocalDate DATE = LocalDate.of(2026, 11, 17);
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 11, 17, 10, 30);

    private DatabaseConnection databaseConnection;
    private JdbcEntryRepository repository;

    @BeforeEach
    void setUp() throws SQLException {
        databaseConnection = new DatabaseConnection(H2_URL, H2_USER, H2_PASSWORD);
        recreateSchema();
        repository = new JdbcEntryRepository(databaseConnection);
    }

    private void recreateSchema() throws SQLException {
        try (Connection connection = databaseConnection.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(DROP_TABLE_SQL);
            statement.execute(readSchema());
        }
    }

    private String readSchema() {
        try (InputStream input = getClass().getResourceAsStream(SCHEMA_RESOURCE)) {
            if (input == null) {
                throw new IllegalStateException(SCHEMA_RESOURCE + " not found on classpath");
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private DiaryEntry.Builder entryBuilder(long userId) {
        return new DiaryEntry.Builder()
                .userId(userId)
                .title(TITLE)
                .content(CONTENT)
                .date(DATE)
                .createdAt(CREATED_AT);
    }

    private DiaryEntry addEntry(long userId) {
        return repository.add(entryBuilder(userId).build());
    }

    // --- add ---

    @Test
    void add_returnsEntryWithGeneratedId() {
        DiaryEntry saved = addEntry(USER_ID);

        assertNotEquals(DiaryEntry.NOT_SAVED_ID, saved.getId());
    }

    @Test
    void add_twoEntries_getDifferentIds() {
        DiaryEntry first = addEntry(USER_ID);
        DiaryEntry second = addEntry(USER_ID);

        assertNotEquals(first.getId(), second.getId());
    }

    // --- findById ---

    @Test
    void findById_afterAdd_returnsEqualEntry() {
        DiaryEntry saved = addEntry(USER_ID);

        DiaryEntry loaded = repository.findById(saved.getId(), USER_ID).orElseThrow();

        assertEquals(saved, loaded);
    }

    @Test
    void findById_keepsStatus() {
        DiaryEntry saved = repository.add(entryBuilder(USER_ID).status(EntryStatus.ARCHIVED).build());

        DiaryEntry loaded = repository.findById(saved.getId(), USER_ID).orElseThrow();

        assertEquals(EntryStatus.ARCHIVED, loaded.getStatus());
    }

    @Test
    void findById_keepsTitleOfMaxLength() {
        String longTitle = "a".repeat(DiaryEntry.MAX_TITLE_LENGTH);
        DiaryEntry saved = repository.add(entryBuilder(USER_ID).title(longTitle).build());

        DiaryEntry loaded = repository.findById(saved.getId(), USER_ID).orElseThrow();

        assertEquals(longTitle, loaded.getTitle());
    }

    @Test
    void findById_keepsCyrillicQuotesAndSqlCharacters() {
        String trickyTitle = "Це'й \"тест\"; DROP TABLE entry; --";
        String trickyContent = "Рядок 1\nРядок 2 'лапки' \"подвійні\" ї є і ґ";
        DiaryEntry saved = repository.add(
                entryBuilder(USER_ID).title(trickyTitle).content(trickyContent).build());

        DiaryEntry loaded = repository.findById(saved.getId(), USER_ID).orElseThrow();

        assertEquals(trickyTitle, loaded.getTitle());
        assertEquals(trickyContent, loaded.getContent());
    }

    @Test
    void findById_unknownId_returnsEmpty() {
        Optional<DiaryEntry> result = repository.findById(UNKNOWN_ID, USER_ID);

        assertTrue(result.isEmpty());
    }

    @Test
    void findById_entryOfOtherUser_returnsEmpty() {
        DiaryEntry saved = addEntry(USER_ID);

        Optional<DiaryEntry> result = repository.findById(saved.getId(), OTHER_USER_ID);

        assertTrue(result.isEmpty());
    }

    // --- findAllByUserId ---

    @Test
    void findAllByUserId_emptyDatabase_returnsEmptyList() {
        List<DiaryEntry> result = repository.findAllByUserId(USER_ID);

        assertTrue(result.isEmpty());
    }

    @Test
    void findAllByUserId_returnsAllEntriesOfUser() {
        DiaryEntry first = addEntry(USER_ID);
        DiaryEntry second = addEntry(USER_ID);

        List<DiaryEntry> result = repository.findAllByUserId(USER_ID);

        assertEquals(2, result.size());
        assertTrue(result.contains(first));
        assertTrue(result.contains(second));
    }

    @Test
    void findAllByUserId_doesNotReturnEntriesOfOtherUsers() {
        DiaryEntry own = addEntry(USER_ID);
        addEntry(OTHER_USER_ID);

        List<DiaryEntry> result = repository.findAllByUserId(USER_ID);

        assertEquals(List.of(own), result);
    }

    @Test
    void findAllByUserId_ordersNewestDateFirst() {
        DiaryEntry older = repository.add(entryBuilder(USER_ID).date(DATE.minusDays(5)).build());
        DiaryEntry newer = repository.add(entryBuilder(USER_ID).date(DATE).build());

        List<DiaryEntry> result = repository.findAllByUserId(USER_ID);

        assertEquals(List.of(newer, older), result);
    }

    @Test
    void findAllByUserId_sameDate_laterAddedFirst() {
        DiaryEntry first = addEntry(USER_ID);
        DiaryEntry second = addEntry(USER_ID);

        List<DiaryEntry> result = repository.findAllByUserId(USER_ID);

        assertEquals(List.of(second, first), result);
    }
}