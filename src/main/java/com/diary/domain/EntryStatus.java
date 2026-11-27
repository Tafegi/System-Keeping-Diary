package com.diary.domain;

/**
 * Статус запису щоденника. При записі в базу даних JDBC-репозиторій
 * зберігає назву відповідної константи.
 */
public enum EntryStatus {

    /** Чернетка запису. */
    DRAFT,

    /** Збережений запис. Початковий статус у {@link DiaryEntry.Builder}. */
    SAVED,

    /** Запис, позначений як архівний. */
    ARCHIVED
}