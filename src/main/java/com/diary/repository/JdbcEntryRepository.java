package com.diary.repository;

import com.diary.domain.DiaryEntry;
import com.diary.domain.EntryStatus;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcEntryRepository implements EntryRepository {

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_CONTENT = "content";
    private static final String COLUMN_ENTRY_DATE = "entry_date";
    private static final String COLUMN_STATUS = "status";
    private static final String COLUMN_CREATED_AT = "created_at";

    private static final String INSERT_SQL =
            "INSERT INTO entry (user_id, title, content, entry_date, status, created_at) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SELECT_COLUMNS =
            "SELECT id, user_id, title, content, entry_date, status, created_at FROM entry ";

    private static final String SELECT_BY_ID_SQL =
            SELECT_COLUMNS + "WHERE id = ? AND user_id = ?";

    private static final String SELECT_ALL_BY_USER_SQL =
            SELECT_COLUMNS + "WHERE user_id = ? ORDER BY entry_date DESC, id DESC";

    private final DatabaseConnection databaseConnection;

    public JdbcEntryRepository(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    @Override
    public DiaryEntry add(DiaryEntry entry) {
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            bindEntryFields(statement, entry);
            statement.executeUpdate();
            return copyWithId(entry, readGeneratedId(statement));
        } catch (SQLException e) {
            throw new RepositoryException("Failed to add entry for user " + entry.getUserId(), e);
        }
    }

    @Override
    public Optional<DiaryEntry> findById(long id, long userId) {
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID_SQL)) {
            statement.setLong(1, id);
            statement.setLong(2, userId);
            try (ResultSet rows = statement.executeQuery()) {
                if (rows.next()) {
                    return Optional.of(mapRow(rows));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find entry " + id + " for user " + userId, e);
        }
    }

    @Override
    public List<DiaryEntry> findAllByUserId(long userId) {
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL_BY_USER_SQL)) {
            statement.setLong(1, userId);
            try (ResultSet rows = statement.executeQuery()) {
                List<DiaryEntry> entries = new ArrayList<>();
                while (rows.next()) {
                    entries.add(mapRow(rows));
                }
                return entries;
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to load entries for user " + userId, e);
        }
    }

    private void bindEntryFields(PreparedStatement statement, DiaryEntry entry) throws SQLException {
        statement.setLong(1, entry.getUserId());
        statement.setString(2, entry.getTitle());
        statement.setString(3, entry.getContent());
        statement.setDate(4, Date.valueOf(entry.getDate()));
        statement.setString(5, entry.getStatus().name());
        statement.setTimestamp(6, Timestamp.valueOf(entry.getCreatedAt()));
    }

    private long readGeneratedId(PreparedStatement statement) throws SQLException {
        try (ResultSet keys = statement.getGeneratedKeys()) {
            if (!keys.next()) {
                throw new RepositoryException("Database did not return a generated id");
            }
            return keys.getLong(1);
        }
    }

    private DiaryEntry mapRow(ResultSet row) throws SQLException {
        return new DiaryEntry.Builder()
                .id(row.getLong(COLUMN_ID))
                .userId(row.getLong(COLUMN_USER_ID))
                .title(row.getString(COLUMN_TITLE))
                .content(row.getString(COLUMN_CONTENT))
                .date(row.getDate(COLUMN_ENTRY_DATE).toLocalDate())
                .status(EntryStatus.valueOf(row.getString(COLUMN_STATUS)))
                .createdAt(row.getTimestamp(COLUMN_CREATED_AT).toLocalDateTime())
                .build();
    }

    private DiaryEntry copyWithId(DiaryEntry entry, long id) {
        return new DiaryEntry.Builder()
                .id(id)
                .userId(entry.getUserId())
                .title(entry.getTitle())
                .content(entry.getContent())
                .date(entry.getDate())
                .status(entry.getStatus())
                .createdAt(entry.getCreatedAt())
                .build();
    }
}