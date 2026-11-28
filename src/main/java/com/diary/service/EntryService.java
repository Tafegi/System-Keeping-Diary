package com.diary.service;

import com.diary.domain.DiaryEntry;
import com.diary.repository.EntryRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Створює записи щоденника та надає доступ до записів указаного користувача.
 * Для збереження й пошуку використовує {@link EntryRepository}, а дату
 * й час створення визначає за переданим {@link Clock}.
 */
public class EntryService {

    private final EntryRepository repository;
    private final Clock clock;

    /**
     * Створює сервіс із заданим репозиторієм і джерелом часу.
     *
     * @param repository репозиторій для збереження та читання записів
     * @param clock годинник, який визначає поточний час і часовий пояс
     */
    public EntryService(EntryRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    /**
     * Створює та зберігає запис щоденника для вказаного користувача.
     * Дату запису та час створення визначає за одним показом годинника.
     *
     * @param userId ідентифікатор власника запису
     * @param title заголовок запису
     * @param content повний текст запису
     * @return збережений запис з ідентифікатором, поверненим репозиторієм
     * @throws IllegalArgumentException якщо ідентифікатор власника недодатний,
     *         заголовок перевищує допустиму довжину або заголовок чи текст
     *         дорівнює {@code null}, порожній чи містить лише пропуски
     */
    public DiaryEntry createEntry(long userId, String title, String content) {
        LocalDateTime now = LocalDateTime.now(clock);
        DiaryEntry entry = new DiaryEntry.Builder()
                .userId(userId)
                .title(title)
                .content(content)
                .date(now.toLocalDate())
                .createdAt(now)
                .build();
        return repository.add(entry);
    }

    /**
     * Шукає запис за його ідентифікатором серед записів указаного власника.
     *
     * @param id ідентифікатор запису
     * @param userId ідентифікатор власника
     * @return знайдений запис або {@link Optional#empty()}, якщо запису немає
     *         чи він належить іншому користувачу
     */
    public Optional<DiaryEntry> findById(long id, long userId) {
        return repository.findById(id, userId);
    }

    /**
     * Повертає всі записи користувача в порядку від найновішої дати до найстарішої.
     * Для записів з однаковою датою першим іде запис із більшим ідентифікатором.
     *
     * @param userId ідентифікатор власника записів
     * @return впорядкований список або порожній список, якщо користувач ще не має записів
     */
    public List<DiaryEntry> findAllByUserId(long userId) {
        return repository.findAllByUserId(userId);
    }
}