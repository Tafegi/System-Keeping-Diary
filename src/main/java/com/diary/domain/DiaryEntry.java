package com.diary.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class DiaryEntry {

    public static final int MAX_TITLE_LENGTH = 100;
    public static final long NOT_SAVED_ID = 0;

    private final long id;
    private final long userId;
    private final String title;
    private final String content;
    private final LocalDate date;
    private final EntryStatus status;
    private final LocalDateTime createdAt;

    private DiaryEntry(Builder builder) {
        this.id = builder.id;
        this.userId = builder.userId;
        this.title = builder.title;
        this.content = builder.content;
        this.date = builder.date;
        this.status = builder.status;
        this.createdAt = builder.createdAt;
    }

    public long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public LocalDate getDate() {
        return date;
    }

    public EntryStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DiaryEntry that = (DiaryEntry) o;
        return id == that.id && userId == that.userId && Objects.equals(title, that.title) && Objects.equals(content, that.content) && Objects.equals(date, that.date) && status == that.status && Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, title, content, date, status, createdAt);
    }

    @Override
    public String toString() {
        return "DiaryEntry{" +
                "id=" + id +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", date=" + date +
                ", status=" + status +
                '}';
    }

    public static final class Builder {
        private long id = NOT_SAVED_ID;
        private long userId;
        private String title;
        private String content;
        private LocalDate date;
        private EntryStatus status = EntryStatus.SAVED;
        private LocalDateTime createdAt = LocalDateTime.now();

        public Builder id(long id) {
            this.id = id;
            return this;
        }

        public Builder userId(long userId) {
            this.userId = userId;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public Builder status(EntryStatus status) {
            this.status = status;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }


        public DiaryEntry build() {
            validate();
            return new DiaryEntry(this);
        }

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
