package com.diary.repository;

import com.diary.domain.DiaryEntry;

import java.util.List;
import java.util.Optional;

public interface EntryRepository {
    DiaryEntry add(DiaryEntry diaryEntry);
    Optional<DiaryEntry> findById(long id, long userId);
    List<DiaryEntry> findAllByUserId(long userId);
}
