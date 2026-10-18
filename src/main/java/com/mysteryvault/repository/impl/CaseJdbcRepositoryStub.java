package com.mysteryvault.repository.impl;

import com.mysteryvault.model.Case;
import com.mysteryvault.repository.GenericRepository;

import java.util.*;

/**
 * JDBC Repository Stub for future database integration.
 * Owned by Member 3 (Module 12: Diagnostics / JDBC Stub).
 * Explicitly a stub — does not claim JDBC is currently active.
 */
public class CaseJdbcRepositoryStub implements GenericRepository<Case, String> {

    private final String connectionUrl;

    public CaseJdbcRepositoryStub() {
        this("jdbc:postgresql://localhost:5432/mysteryvault_stub");
    }

    public CaseJdbcRepositoryStub(String connectionUrl) {
        this.connectionUrl = connectionUrl;
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    @Override
    public Case save(Case entity) {
        throw new UnsupportedOperationException("JDBC Adapter is an unactivated stub. Use CaseMemoryRepository for runtime persistence.");
    }

    @Override
    public Optional<Case> findById(String id) {
        throw new UnsupportedOperationException("JDBC Adapter is an unactivated stub. Use CaseMemoryRepository for runtime persistence.");
    }

    @Override
    public List<Case> findAll() {
        return Collections.emptyList();
    }

    @Override
    public boolean deleteById(String id) {
        throw new UnsupportedOperationException("JDBC Adapter is an unactivated stub.");
    }

    @Override
    public boolean existsById(String id) {
        return false;
    }

    @Override
    public long count() {
        return 0L;
    }

    public void clear() {
        // stub no-op
    }
}
