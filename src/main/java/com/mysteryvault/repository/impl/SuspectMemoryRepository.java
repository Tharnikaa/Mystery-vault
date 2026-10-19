package com.mysteryvault.repository.impl;

import com.mysteryvault.model.Suspect;
import com.mysteryvault.repository.SuspectRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Thread-safe in-memory implementation of SuspectRepository for unit testing.
 */
public class SuspectMemoryRepository implements SuspectRepository {

    private final ConcurrentHashMap<String, Suspect> storage = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<String>> caseSuspectLinks = new ConcurrentHashMap<>();

    @Override
    public Suspect save(Suspect entity) {
        Objects.requireNonNull(entity, "Suspect cannot be null");
        Objects.requireNonNull(entity.getId(), "Suspect ID cannot be null");
        String id = entity.getId().trim();
        if (id.isEmpty()) {
            throw new IllegalArgumentException("Suspect ID cannot be empty");
        }
        storage.put(id, entity);
        return entity;
    }

    @Override
    public Optional<Suspect> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(id.trim()));
    }

    @Override
    public List<Suspect> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<Suspect> findByCaseId(String caseId) {
        if (caseId == null || caseId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> linkedIds = caseSuspectLinks.get(caseId.trim());
        if (linkedIds == null || linkedIds.isEmpty()) {
            return Collections.emptyList();
        }
        return linkedIds.stream()
                .map(storage::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<Suspect> searchByName(String nameQuery) {
        if (nameQuery == null || nameQuery.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String q = nameQuery.trim().toLowerCase();
        return storage.values().stream()
                .filter(s -> s.getName() != null && s.getName().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    @Override
    public void linkSuspectToCase(String caseId, String suspectId) {
        Objects.requireNonNull(caseId, "Case ID cannot be null");
        Objects.requireNonNull(suspectId, "Suspect ID cannot be null");
        caseSuspectLinks.computeIfAbsent(caseId.trim(), k -> ConcurrentHashMap.newKeySet()).add(suspectId.trim());
    }

    @Override
    public void unlinkSuspectFromCase(String caseId, String suspectId) {
        if (caseId == null || suspectId == null) return;
        Set<String> set = caseSuspectLinks.get(caseId.trim());
        if (set != null) {
            set.remove(suspectId.trim());
        }
    }

    @Override
    public boolean deleteById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        String trimmed = id.trim();
        for (Set<String> links : caseSuspectLinks.values()) {
            links.remove(trimmed);
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
        caseSuspectLinks.clear();
    }
}
