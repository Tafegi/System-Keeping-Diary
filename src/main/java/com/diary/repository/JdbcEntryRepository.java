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

/**
 * Реалізує {@link EntryRepository} для таблиці {@code entry} за допомогою JDBC.
 * Для кожної операції відкриває з'єднання, виконує параметризований SQL-запит
 * і закриває використані JDBC-ресурси.
 *
 * <p>Помилки JDBC передає викликачу як {@link RepositoryException},
 * зберігаючи початковий {@link SQLException} як причину.
 */
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

    /**
     * Створює репозиторій із заданим джерелом JDBC-з'єднань.
     *
     * @param databaseConnection об'єкт, який відкриває з'єднання для операцій репозиторію
     */
    public JdbcEntryRepository(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Після вставлення рядка читає згенерований ключ і повертає копію
     * переданого запису з цим ключем. Решту полів копіює з вихідного об'єкта.
     *
     * @throws RepositoryException якщо під час операції виникла помилка JDBC
     *         або база даних не повернула згенерований ідентифікатор
     */
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

    /**
     * {@inheritDoc}
     *
     * <p>Перевіряє ідентифікатор запису та його власника в одному SQL-запиті.
     *
     * @throws RepositoryException якщо під час пошуку або читання результату
     *         виникла помилка JDBC
     */
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

    /**
     * {@inheritDoc}
     *
     * <p>Вибирає рядки за {@code user_id} та впорядковує їх у базі даних
     * за {@code entry_date DESC, id DESC}.
     *
     * @throws RepositoryException якщо під час отримання списку виникла помилка JDBC
     */
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

    /**
     * Заповнює шість параметрів запиту INSERT у порядку стовпців таблиці.
     * Статус передає як назву значення enum, дату запису — як SQL DATE,
     * а час створення — як SQL TIMESTAMP.
     *
     * @param statement підготовлений запит на вставлення запису
     * @param entry запис, значення полів якого передаються в запит
     * @throws SQLException якщо JDBC-драйверу не вдалося встановити параметр
     */
    private void bindEntryFields(PreparedStatement statement, DiaryEntry entry) throws SQLException {
        statement.setLong(1, entry.getUserId());
        statement.setString(2, entry.getTitle());
        statement.setString(3, entry.getContent());
        statement.setDate(4, Date.valueOf(entry.getDate()));
        statement.setString(5, entry.getStatus().name());
        statement.setTimestamp(6, Timestamp.valueOf(entry.getCreatedAt()));
    }

    /**
     * Читає ідентифікатор із першого рядка згенерованих ключів після INSERT.
     *
     * @param statement виконаний запит, підготовлений для повернення згенерованих ключів
     * @return ідентифікатор нового запису
     * @throws SQLException якщо виникла помилка JDBC під час читання ключів
     * @throws RepositoryException якщо запит не повернув жодного ключа
     */
    private long readGeneratedId(PreparedStatement statement) throws SQLException {
        try (ResultSet keys = statement.getGeneratedKeys()) {
            if (!keys.next()) {
                throw new RepositoryException("Database did not return a generated id");
            }
            return keys.getLong(1);
        }
    }

    /**
     * Створює запис щоденника зі значень поточного рядка результату.
     * Курсор має вже вказувати на рядок, який потрібно прочитати.
     *
     * @param row результат SQL-запиту з полями запису щоденника
     * @return об'єкт {@link DiaryEntry}, відновлений із поточного рядка
     * @throws SQLException якщо не вдалося прочитати значення стовпця
     * @throws IllegalArgumentException якщо назва статусу не відповідає
     *         жодному значенню {@link EntryStatus} або дані порушують правила побудови запису
     */
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

    /**
     * Створює копію запису з ідентифікатором, який призначила база даних.
     * Власника, заголовок, текст, дату, статус і час створення бере з вихідного запису.
     *
     * @param entry вихідний запис
     * @param id згенерований ідентифікатор
     * @return новий об'єкт {@link DiaryEntry} із заданим ідентифікатором
     */
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