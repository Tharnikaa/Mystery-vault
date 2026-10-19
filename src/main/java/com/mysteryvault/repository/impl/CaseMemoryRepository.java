package com.mysteryvault.repository.impl;

import com.mysteryvault.enums.CaseStatus;
import com.mysteryvault.model.Case;
import com.mysteryvault.repository.GenericRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Thread-safe, in-memory repository implementation for Case entities (Module 1).
 * Uses ConcurrentHashMap for lock-free concurrency and defensive copying on read/write.
 */
public class CaseMemoryRepository implements GenericRepository<Case, String> {

    private final ConcurrentHashMap<String, Case> storage = new ConcurrentHashMap<>();

    @Override
    public Case save(Case entity) {
        Objects.requireNonNull(entity, "Entity cannot be null");
        Objects.requireNonNull(entity.getId(), "Entity ID cannot be null");
        String id = entity.getId().trim();
        if (id.isEmpty()) {
            throw new IllegalArgumentException("Entity ID cannot be empty");
        }

        // Store a defensive copy
        Case copy = new Case(entity);
        storage.put(id, copy);
        return new Case(copy);
    }

    @Override
    public Optional<Case> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        Case found = storage.get(id.trim());
        return found != null ? Optional.of(new Case(found)) : Optional.empty();
    }

    @Override
    public List<Case> findAll() {
        return storage.values().stream()
                .map(Case::new)
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        return storage.remove(id.trim()) != null;
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

    /**
     * Finds all cases with the specified status.
     *
     * @param status Status filter
     * @return List of matching cases
     */
    public List<Case> findByStatus(CaseStatus status) {
        if (status == null) {
            return Collections.emptyList();
        }
        return storage.values().stream()
                .filter(c -> c.getStatus() == status)
                .map(Case::new)
                .collect(Collectors.toList());
    }

    /**
     * Clears all stored cases (useful for test isolation).
     */
    public void clear() {
        storage.clear();
    }
}
