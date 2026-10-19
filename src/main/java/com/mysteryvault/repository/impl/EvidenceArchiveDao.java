package com.mysteryvault.repository.impl;

import com.mysteryvault.config.DatabaseConnectionFactory;
import com.mysteryvault.exception.DataAccessException;
import com.mysteryvault.model.Evidence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Evidence Archive DAO (Module 3 & Module 12).
 * Supports database-backed archival in table evidence_archive with in-memory caching fallback.
 */
public class EvidenceArchiveDao {

    private final DatabaseConnectionFactory connectionFactory;
    private final Map<String, Evidence> inMemoryArchive = new HashMap<>();

    public EvidenceArchiveDao() {
        this(null);
    }

    public EvidenceArchiveDao(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void archive(Evidence evidence) {
        if (evidence == null) {
            return;
        }

        inMemoryArchive.put(evidence.getId(), evidence);

        if (connectionFactory != null) {
            String sql = "INSERT INTO evidence_archive (archive_id, evidence_id, case_id, name, summary, archived_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = connectionFactory.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, UUID.randomUUID().toString());
                ps.setString(2, evidence.getId());
                ps.setString(3, evidence.getCaseId());
                ps.setString(4, evidence.getName());
                ps.setString(5, evidence.getSummary());
                ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new DataAccessException("Failed to archive evidence in database: " + e.getMessage(), e);
            }
        }
    }

    public Optional<Evidence> retrieve(String evidenceId) {
        if (evidenceId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(inMemoryArchive.get(evidenceId));
    }

    public List<Evidence> listAllArchived() {
        return new ArrayList<>(inMemoryArchive.values());
    }

    public int count() {
        if (connectionFactory != null) {
            String sql = "SELECT COUNT(*) FROM evidence_archive";
            try (Connection conn = connectionFactory.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            } catch (SQLException e) {
                // fallback to memory size on error
            }
        }
        return inMemoryArchive.size();
    }
}
