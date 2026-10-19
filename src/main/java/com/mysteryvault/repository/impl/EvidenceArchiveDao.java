package com.mysteryvault.repository.impl;

import com.mysteryvault.model.Evidence;

import java.util.*;

/**
 * Evidence Archive DAO Interface / Stub.
 * Owned by Member 3 (Module 12: Diagnostics / JDBC Stub).
 * Future persistence adapter for external long-term evidence storage.
 */
public class EvidenceArchiveDao {

    private final Map<String, Evidence> mockArchive = new HashMap<>();

    public EvidenceArchiveDao() {
    }

    public void archive(Evidence evidence) {
        if (evidence != null) {
            mockArchive.put(evidence.getId(), evidence);
        }
    }

    public Optional<Evidence> retrieve(String evidenceId) {
        return Optional.ofNullable(mockArchive.get(evidenceId));
    }

    public List<Evidence> listAllArchived() {
        return new ArrayList<>(mockArchive.values());
    }

    public int count() {
        return mockArchive.size();
    }
}
