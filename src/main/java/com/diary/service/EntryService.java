package com.diary.service;

import com.diary.domain.DiaryEntry;
import com.diary.repository.EntryRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class EntryService {
    private final EntryRepository repository;
    private final Clock clock;


    public EntryService(EntryRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

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

    public Optional<DiaryEntry> findById(long id, long userId) {
        return repository.findById(id, userId);
    }

    public List<DiaryEntry> findAllByUserId(long userId) {
        return repository.findAllByUserId(userId);
    }

}
