package com.mysteryvault.repository.impl;

import com.mysteryvault.model.Evidence;
import com.mysteryvault.repository.EvidenceRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of EvidenceRepository for isolated unit testing.
 */
public class EvidenceMemoryRepository implements EvidenceRepository {

    private final ConcurrentHashMap<String, Evidence> storage = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<String>> caseEvidenceLinks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Boolean> discoveredFlags = new ConcurrentHashMap<>();

    @Override
    public Evidence save(Evidence entity) {
        Objects.requireNonNull(entity, "Evidence cannot be null");
        Objects.requireNonNull(entity.getId(), "Evidence ID cannot be null");
        String id = entity.getId().trim();
        if (id.isEmpty()) {
            throw new IllegalArgumentException("Evidence ID cannot be empty");
        }
        storage.put(id, entity);
        if (entity.getCaseId() != null && !entity.getCaseId().trim().isEmpty()) {
            linkEvidenceToCase(entity.getCaseId().trim(), id);
        }
        return entity;
    }

    @Override
    public Optional<Evidence> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(id.trim()));
    }

    @Override
    public List<Evidence> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<Evidence> findByCaseId(String caseId) {
        if (caseId == null || caseId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return storage.values().stream()
                .filter(e -> caseId.trim().equalsIgnoreCase(e.getCaseId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Evidence> findDiscoveredByCaseId(String caseId) {
        if (caseId == null || caseId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return storage.values().stream()
                .filter(e -> caseId.trim().equalsIgnoreCase(e.getCaseId()))
                .filter(e -> Boolean.TRUE.equals(discoveredFlags.get(e.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public void markDiscovered(String evidenceId, boolean discovered) {
        if (evidenceId != null) {
            discoveredFlags.put(evidenceId.trim(), discovered);
        }
    }

    @Override
    public void linkEvidenceToCase(String caseId, String evidenceId) {
        Objects.requireNonNull(caseId, "Case ID cannot be null");
        Objects.requireNonNull(evidenceId, "Evidence ID cannot be null");
        caseEvidenceLinks.computeIfAbsent(caseId.trim(), k -> ConcurrentHashMap.newKeySet()).add(evidenceId.trim());
    }

    @Override
    public boolean deleteById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        String trimmed = id.trim();
        discoveredFlags.remove(trimmed);
        for (Set<String> set : caseEvidenceLinks.values()) {
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
        caseEvidenceLinks.clear();
        discoveredFlags.clear();
    }
}
