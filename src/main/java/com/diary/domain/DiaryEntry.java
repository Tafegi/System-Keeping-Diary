package com.diary.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Запис особистого щоденника з власником, заголовком, текстом і статусом.
 * Дата запису вказує, до якого дня він належить, а час створення фіксує
 * момент його створення з точністю до локальної дати й часу.
 *
 * <p>Запис створюється через {@link Builder}. Після створення значення
 * його полів незмінні.
 */
public class DiaryEntry {

    /**
     * Максимальна довжина заголовка в одиницях UTF-16 за {@link String#length()}.
     */
    public static final int MAX_TITLE_LENGTH = 100;

    /**
     * Ідентифікатор нового запису, якому база даних ще не призначила ID.
     */
    public static final long NOT_SAVED_ID = 0;

    private final long id;
    private final long userId;
    private final String title;
    private final String content;
    private final LocalDate date;
    private final EntryStatus status;
    private final LocalDateTime createdAt;

    /**
     * Створює запис зі значень, перевірених під час виклику {@link Builder#build()}.
     *
     * @param builder побудовник із підготовленими значеннями полів
     */
    private DiaryEntry(Builder builder) {
        this.id = builder.id;
        this.userId = builder.userId;
        this.title = builder.title;
        this.content = builder.content;
        this.date = builder.date;
        this.status = builder.status;
        this.createdAt = builder.createdAt;
    }

    /**
     * @return ідентифікатор запису або {@link #NOT_SAVED_ID} для нового запису
     */
    public long getId() {
        return id;
    }

    /**
     * @return ідентифікатор користувача, якому належить запис
     */
    public long getUserId() {
        return userId;
    }

    /**
     * @return заголовок запису
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return повний текст запису зі збереженими переносами рядків
     */
    public String getContent() {
        return content;
    }

    /**
     * @return календарна дата, до якої належить запис
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * @return статус запису
     */
    public EntryStatus getStatus() {
        return status;
    }

    /**
     * @return локальна дата й час створення запису
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Порівнює записи за всіма полями, включно зі статусом і часом створення.
     *
     * @param o об'єкт для порівняння
     * @return {@code true}, якщо об'єкти мають однаковий клас і значення всіх полів
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        DiaryEntry that = (DiaryEntry) o;
        return id == that.id && userId == that.userId && Objects.equals(title, that.title) && Objects.equals(content, that.content) && Objects.equals(date, that.date) && status == that.status && Objects.equals(createdAt, that.createdAt);
    }

    /**
     * Обчислює хеш-код за тими самими полями, які порівнює {@link #equals(Object)}.
     *
     * @return хеш-код запису
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, userId, title, content, date, status, createdAt);
    }

    /**
     * Формує коротке текстове представлення запису.
     *
     * @return рядок з ідентифікаторами запису й власника, заголовком, датою та статусом
     */
    @Override
    public String toString() {
        return "DiaryEntry{" + "id=" + id + ", userId=" + userId + ", title='" + title + '\'' + ", date=" + date + ", status=" + status + '}';
    }

    /**
     * Побудовник для створення {@link DiaryEntry}. Для нового запису потрібно
     * задати власника, заголовок, текст і дату. За замовчуванням ідентифікатор
     * дорівнює {@link DiaryEntry#NOT_SAVED_ID}, статус — {@link EntryStatus#SAVED},
     * а час створення визначається під час створення самого побудовника.
     * <p>
     * <p>Реалізує породжувальний патерн Builder: поля запису задаються
     * послідовними викликами методів, а {@link #build()} перевіряє їх
     * і створює готовий {@link DiaryEntry}.
     *
     * <p>Методи налаштування можна викликати ланцюжком. Значення перевіряються
     * під час виклику {@link #build()}.
     */
    public static final class Builder {

        private long id = NOT_SAVED_ID;
        private long userId;
        private String title;
        private String content;
        private LocalDate date;
        private EntryStatus status = EntryStatus.SAVED;
        private LocalDateTime createdAt = LocalDateTime.now();

        /**
         * Задає ідентифікатор запису, наприклад під час його читання з бази даних.
         *
         * @param id невід'ємний ідентифікатор запису
         * @return цей побудовник
         */
        public Builder id(long id) {
            this.id = id;
            return this;
        }

        /**
         * Задає власника запису.
         *
         * @param userId додатний ідентифікатор користувача
         * @return цей побудовник
         */
        public Builder userId(long userId) {
            this.userId = userId;
            return this;
        }

        /**
         * Задає заголовок запису.
         *
         * @param title заголовок із принаймні одним непробільним символом
         * @return цей побудовник
         */
        public Builder title(String title) {
            this.title = title;
            return this;
        }

        /**
         * Задає повний текст запису.
         *
         * @param content текст із принаймні одним непробільним символом
         * @return цей побудовник
         */
        public Builder content(String content) {
            this.content = content;
            return this;
        }

        /**
         * Задає день, до якого належить запис щоденника.
         *
         * @param date календарна дата запису
         * @return цей побудовник
         */
        public Builder date(LocalDate date) {
            this.date = date;
            return this;
        }

        /**
         * Задає статус запису.
         *
         * @param status статус запису щоденника
         * @return цей побудовник
         */
        public Builder status(EntryStatus status) {
            this.status = status;
            return this;
        }

        /**
         * Задає час створення запису. Дає змогу використати час, визначений
         * сервісом, або відновити значення, прочитане з бази даних.
         *
         * @param createdAt локальна дата й час створення
         * @return цей побудовник
         */
        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        /**
         * Перевіряє підготовлені значення та створює з них запис щоденника.
         *
         * @return новий запис із поточними значеннями полів побудовника
         * @throws IllegalArgumentException якщо обов'язкове поле відсутнє
         *                                  або його значення порушує обмеження запису
         */
        public DiaryEntry build() {
            validate();
            return new DiaryEntry(this);
        }

        /**
         * Перевіряє, що ідентифікатор запису невід'ємний, а ідентифікатор
         * власника додатний. Заголовок і текст мають містити хоча б один
         * непробільний символ. Довжина заголовка обмежена
         * {@link DiaryEntry#MAX_TITLE_LENGTH}. Дата, статус і час створення є обов'язковими.
         *
         * @throws IllegalArgumentException якщо порушено хоча б одну з цих умов
         */
        private void validate() {
            if (this.id < 0) {
                throw new IllegalArgumentException("id must not be negative");
            }
            if (this.userId <= 0) {
                throw new IllegalArgumentException("userId must be greater than 0");
            }
            if (this.title == null || this.title.isBlank()) {
                throw new IllegalArgumentException("title is null or empty");
            }
            if (this.title.length() > MAX_TITLE_LENGTH) {
                throw new IllegalArgumentException("title exceeds max length");
            }
            if (this.content == null || this.content.isBlank()) {
                throw new IllegalArgumentException("content is null or empty");
            }
            if (this.date == null) {
                throw new IllegalArgumentException("date must not be null");
            }
            if (this.status == null) {
                throw new IllegalArgumentException("status is null");
            }
            if (this.createdAt == null) {
                throw new IllegalArgumentException("createdAt is null");
            }
        }
    }
}