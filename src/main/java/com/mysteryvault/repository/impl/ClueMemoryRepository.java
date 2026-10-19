package com.mysteryvault.repository.impl;

import com.mysteryvault.model.Clue;
import com.mysteryvault.repository.ClueRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of ClueRepository for fast, isolated unit testing.
 */
public class ClueMemoryRepository implements ClueRepository {

    private final ConcurrentHashMap<String, Clue> storage = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<String>> prerequisites = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<String>> caseClueLinks = new ConcurrentHashMap<>();

    @Override
    public Clue save(Clue entity) {
        Objects.requireNonNull(entity, "Clue cannot be null");
        Objects.requireNonNull(entity.getId(), "Clue ID cannot be null");
        String id = entity.getId().trim();
        if (id.isEmpty()) {
            throw new IllegalArgumentException("Clue ID cannot be empty");
        }
        storage.put(id, entity);
        if (entity.getRequiredEvidenceIds() != null) {
            prerequisites.put(id, new HashSet<>(entity.getRequiredEvidenceIds()));
        }
        if (entity.getCaseId() != null && !entity.getCaseId().trim().isEmpty()) {
            linkClueToCase(entity.getCaseId().trim(), id);
        }
        return entity;
    }

    @Override
    public Optional<Clue> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        Clue found = storage.get(id.trim());
        if (found != null) {
            Set<String> prereqs = prerequisites.getOrDefault(id.trim(), Collections.emptySet());
            found.setRequiredEvidenceIds(prereqs);
            return Optional.of(found);
        }
        return Optional.empty();
    }

    @Override
    public List<Clue> findAll() {
        return storage.values().stream().map(c -> {
            Set<String> prereqs = prerequisites.getOrDefault(c.getId(), Collections.emptySet());
            c.setRequiredEvidenceIds(prereqs);
            return c;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Clue> findByCaseId(String caseId) {
        if (caseId == null || caseId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return storage.values().stream()
                .filter(c -> caseId.trim().equalsIgnoreCase(c.getCaseId()))
                .peek(c -> c.setRequiredEvidenceIds(prerequisites.getOrDefault(c.getId(), Collections.emptySet())))
                .collect(Collectors.toList());
    }

    @Override
    public List<Clue> findUnlockedByCaseId(String caseId) {
        if (caseId == null || caseId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return storage.values().stream()
                .filter(c -> caseId.trim().equalsIgnoreCase(c.getCaseId()) && c.isUnlocked())
                .peek(c -> c.setRequiredEvidenceIds(prerequisites.getOrDefault(c.getId(), Collections.emptySet())))
                .collect(Collectors.toList());
    }

    @Override
    public void savePrerequisites(String clueId, Set<String> requiredEvidenceIds) {
        Objects.requireNonNull(clueId, "Clue ID cannot be null");
        prerequisites.put(clueId.trim(), requiredEvidenceIds != null ? new HashSet<>(requiredEvidenceIds) : new HashSet<>());
    }

    @Override
    public Set<String> findPrerequisites(String clueId) {
        if (clueId == null || clueId.trim().isEmpty()) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(prerequisites.getOrDefault(clueId.trim(), Collections.emptySet()));
    }

    @Override
    public void updateUnlockedState(String clueId, boolean unlocked, LocalDateTime unlockedAt) {
        Objects.requireNonNull(clueId, "Clue ID cannot be null");
        Clue c = storage.get(clueId.trim());
        if (c != null) {
            c.setUnlocked(unlocked);
            c.setUnlockedAt(unlockedAt);
        }
    }

    @Override
    public void linkClueToCase(String caseId, String clueId) {
        Objects.requireNonNull(caseId, "Case ID cannot be null");
        Objects.requireNonNull(clueId, "Clue ID cannot be null");
        caseClueLinks.computeIfAbsent(caseId.trim(), k -> ConcurrentHashMap.newKeySet()).add(clueId.trim());
    }

    @Override
    public boolean deleteById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        String trimmed = id.trim();
        prerequisites.remove(trimmed);
        for (Set<String> set : caseClueLinks.values()) {
            set.remove(trimmed);
        }
        return storage.remove(trimmed) != null;
    }

    @Override
    public boolean existsById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        return storage.containsKey(id.trim());
    }

    @Override
    public long count() {
        return storage.size();
    }

    public void clear() {
        storage.clear();
        prerequisites.clear();
        caseClueLinks.clear();
    }
}
