package com.diary.repository;

import com.diary.domain.DiaryEntry;

import java.util.List;
import java.util.Optional;

/**
 * Визначає операції збереження та читання записів щоденника.
 * Під час читання ідентифікатор користувача обмежує результат його записами.
 */
public interface EntryRepository {

    /**
     * Зберігає новий запис щоденника.
     *
     * @param diaryEntry запис для збереження
     * @return запис з ідентифікатором, призначеним під час збереження
     */
    DiaryEntry add(DiaryEntry diaryEntry);

    /**
     * Шукає запис за його ідентифікатором серед записів указаного власника.
     *
     * @param id ідентифікатор запису
     * @param userId ідентифікатор власника
     * @return знайдений запис або {@link Optional#empty()}, якщо запису немає
     *         чи він належить іншому користувачу
     */
    Optional<DiaryEntry> findById(long id, long userId);

    /**
     * Повертає всі записи користувача від найновішої дати до найстарішої.
     * Серед записів з однаковою датою першим іде запис із більшим ідентифікатором.
     *
     * @param userId ідентифікатор власника записів
     * @return впорядкований список або порожній список, якщо записів немає
     */
    List<DiaryEntry> findAllByUserId(long userId);
}