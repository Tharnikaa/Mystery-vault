package com.mysteryvault.repository.impl;

import com.mysteryvault.config.DatabaseConnectionFactory;
import com.mysteryvault.enums.CaseStatus;
import com.mysteryvault.exception.DataAccessException;
import com.mysteryvault.model.Case;
import com.mysteryvault.model.Priority;
import com.mysteryvault.repository.GenericRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Production JDBC repository implementation for Case entities (Module 1).
 * Uses prepared statements, transactions, and foreign-key relational hydration.
 */
public class CaseJdbcRepository implements GenericRepository<Case, String> {

    private final DatabaseConnectionFactory connectionFactory;

    public CaseJdbcRepository(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = Objects.requireNonNull(connectionFactory, "DatabaseConnectionFactory cannot be null");
    }

    @Override
    public Case save(Case entity) {
        Objects.requireNonNull(entity, "Case entity cannot be null");
        Objects.requireNonNull(entity.getId(), "Case ID cannot be null");
        String id = entity.getId().trim();
        if (id.isEmpty()) {
            throw new IllegalArgumentException("Case ID cannot be empty");
        }

        try (Connection conn = connectionFactory.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (existsInternal(conn, id)) {
                    updateInternal(conn, entity);
                } else {
                    insertInternal(conn, entity);
                }

                syncAssociations(conn, entity);

                conn.commit();
                return findByIdInternal(conn, id).orElse(entity);
            } catch (SQLException e) {
                conn.rollback();
                throw new DataAccessException("Failed to persist case '" + id + "': " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database connection error while saving case '" + id + "': " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Case> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        try (Connection conn = connectionFactory.getConnection()) {
            return findByIdInternal(conn, id.trim());
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query case by ID '" + id + "': " + e.getMessage(), e);
        }
    }

    @Override
    public List<Case> findAll() {
        String sql = "SELECT id, title, description, status, priority, lead_investigator, created_at, updated_at FROM cases ORDER BY created_at DESC";
        List<Case> cases = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Case c = mapResultSetToCase(rs);
                hydrateAssociations(conn, c);
                cases.add(c);
            }
            return cases;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to list all cases: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        String sql = "DELETE FROM cases WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete case '" + id + "': " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        try (Connection conn = connectionFactory.getConnection()) {
            return existsInternal(conn, id.trim());
        } catch (SQLException e) {
            throw new DataAccessException("Failed to check existence for case '" + id + "': " + e.getMessage(), e);
        }
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM cases";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0L;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to count cases: " + e.getMessage(), e);
        }
    }

    public List<Case> findByStatus(CaseStatus status) {
        if (status == null) {
            return Collections.emptyList();
        }
        String sql = "SELECT id, title, description, status, priority, lead_investigator, created_at, updated_at FROM cases WHERE status = ? ORDER BY created_at DESC";
        List<Case> cases = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Case c = mapResultSetToCase(rs);
                    hydrateAssociations(conn, c);
                    cases.add(c);
                }
            }
            return cases;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query cases by status '" + status + "': " + e.getMessage(), e);
        }
    }

    private boolean existsInternal(Connection conn, String id) throws SQLException {
        String sql = "SELECT 1 FROM cases WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void insertInternal(Connection conn, Case entity) throws SQLException {
        String sql = "INSERT INTO cases (id, title, description, status, priority, lead_investigator, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getId());
            ps.setString(2, entity.getTitle());
            ps.setString(3, entity.getDescription());
            ps.setString(4, entity.getStatus() != null ? entity.getStatus().name() : CaseStatus.OPEN.name());
            ps.setString(5, entity.getPriority() != null ? entity.getPriority().name() : Priority.MEDIUM.name());
            ps.setString(6, entity.getLeadInvestigator());
            ps.setTimestamp(7, toTimestamp(entity.getCreatedAt()));
            ps.setTimestamp(8, toTimestamp(entity.getUpdatedAt()));
            ps.executeUpdate();
        }
    }

    private void updateInternal(Connection conn, Case entity) throws SQLException {
        String sql = "UPDATE cases SET title = ?, description = ?, status = ?, priority = ?, lead_investigator = ?, updated_at = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getTitle());
            ps.setString(2, entity.getDescription());
            ps.setString(3, entity.getStatus() != null ? entity.getStatus().name() : CaseStatus.OPEN.name());
            ps.setString(4, entity.getPriority() != null ? entity.getPriority().name() : Priority.MEDIUM.name());
            ps.setString(5, entity.getLeadInvestigator());
            ps.setTimestamp(6, toTimestamp(LocalDateTime.now()));
            ps.setString(7, entity.getId());
            ps.executeUpdate();
        }
    }

    private void syncAssociations(Connection conn, Case entity) throws SQLException {
        String caseId = entity.getId();

        // 1. Case-Suspects
        try (PreparedStatement psDel = conn.prepareStatement("DELETE FROM case_suspects WHERE case_id = ?");
             PreparedStatement psIns = conn.prepareStatement("INSERT INTO case_suspects (case_id, suspect_id) VALUES (?, ?) ON CONFLICT DO NOTHING")) {
            psDel.setString(1, caseId);
            psDel.executeUpdate();

            for (String suspectId : entity.getSuspectIds()) {
                if (suspectId != null && !suspectId.trim().isEmpty()) {
                    ensureSuspectStubExists(conn, suspectId.trim());
                    psIns.setString(1, caseId);
                    psIns.setString(2, suspectId.trim());
                    try {
                        psIns.executeUpdate();
                    } catch (SQLException ignore) {
                        // ignore duplicate
                    }
                }
            }
        }

        // 2. Case-Evidence
        try (PreparedStatement psDel = conn.prepareStatement("DELETE FROM case_evidence WHERE case_id = ?");
             PreparedStatement psIns = conn.prepareStatement("INSERT INTO case_evidence (case_id, evidence_id) VALUES (?, ?) ON CONFLICT DO NOTHING")) {
            psDel.setString(1, caseId);
            psDel.executeUpdate();

            for (String evidenceId : entity.getEvidenceIds()) {
                if (evidenceId != null && !evidenceId.trim().isEmpty()) {
                    ensureEvidenceStubExists(conn, caseId, evidenceId.trim());
                    psIns.setString(1, caseId);
                    psIns.setString(2, evidenceId.trim());
                    try {
                        psIns.executeUpdate();
                    } catch (SQLException ignore) {
                        // ignore duplicate
                    }
                }
            }
        }

        // 3. Case-Clues
        try (PreparedStatement psDel = conn.prepareStatement("DELETE FROM case_clues WHERE case_id = ?");
             PreparedStatement psIns = conn.prepareStatement("INSERT INTO case_clues (case_id, clue_id) VALUES (?, ?) ON CONFLICT DO NOTHING")) {
            psDel.setString(1, caseId);
            psDel.executeUpdate();

            for (String clueId : entity.getClueIds()) {
                if (clueId != null && !clueId.trim().isEmpty()) {
                    ensureClueStubExists(conn, caseId, clueId.trim());
                    psIns.setString(1, caseId);
                    psIns.setString(2, clueId.trim());
                    try {
                        psIns.executeUpdate();
                    } catch (SQLException ignore) {
                        // ignore duplicate
                    }
                }
            }
        }
    }

    private void ensureSuspectStubExists(Connection conn, String suspectId) throws SQLException {
        try (PreparedStatement check = conn.prepareStatement("SELECT 1 FROM suspects WHERE id = ?")) {
            check.setString(1, suspectId);
            try (ResultSet rs = check.executeQuery()) {
                if (!rs.next()) {
                    try (PreparedStatement ins = conn.prepareStatement(
                            "INSERT INTO suspects (id, name, role) VALUES (?, ?, ?)")) {
                        ins.setString(1, suspectId);
                        ins.setString(2, "Suspect " + suspectId);
                        ins.setString(3, "Suspect");
                        ins.executeUpdate();
                    }
                }
            }
        }
    }

    private void ensureEvidenceStubExists(Connection conn, String caseId, String evidenceId) throws SQLException {
        try (PreparedStatement check = conn.prepareStatement("SELECT 1 FROM evidence WHERE id = ?")) {
            check.setString(1, evidenceId);
            try (ResultSet rs = check.executeQuery()) {
                if (!rs.next()) {
                    try (PreparedStatement ins = conn.prepareStatement(
                            "INSERT INTO evidence (id, name, case_id, evidence_type) VALUES (?, ?, ?, ?)")) {
                        ins.setString(1, evidenceId);
                        ins.setString(2, "Evidence " + evidenceId);
                        ins.setString(3, caseId);
                        ins.setString(4, "DOCUMENT");
                        ins.executeUpdate();
                    }
                }
            }
        }
    }

    private void ensureClueStubExists(Connection conn, String caseId, String clueId) throws SQLException {
        try (PreparedStatement check = conn.prepareStatement("SELECT 1 FROM clues WHERE id = ?")) {
            check.setString(1, clueId);
            try (ResultSet rs = check.executeQuery()) {
                if (!rs.next()) {
                    try (PreparedStatement ins = conn.prepareStatement(
                            "INSERT INTO clues (id, case_id, title) VALUES (?, ?, ?)")) {
                        ins.setString(1, clueId);
                        ins.setString(2, caseId);
                        ins.setString(3, "Clue " + clueId);
                        ins.executeUpdate();
                    }
                }
            }
        }
    }

    private Optional<Case> findByIdInternal(Connection conn, String id) throws SQLException {
        String sql = "SELECT id, title, description, status, priority, lead_investigator, created_at, updated_at FROM cases WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Case c = mapResultSetToCase(rs);
                    hydrateAssociations(conn, c);
                    return Optional.of(c);
                }
                return Optional.empty();
            }
        }
    }

    private void hydrateAssociations(Connection conn, Case c) throws SQLException {
        // Suspects
        Set<String> suspects = new HashSet<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT suspect_id FROM case_suspects WHERE case_id = ?")) {
            ps.setString(1, c.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    suspects.add(rs.getString("suspect_id"));
                }
            }
        }
        c.setSuspectIds(suspects);

        // Evidence
        Set<String> evidence = new HashSet<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT evidence_id FROM case_evidence WHERE case_id = ?")) {
            ps.setString(1, c.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    evidence.add(rs.getString("evidence_id"));
                }
            }
        }
        c.setEvidenceIds(evidence);

        // Clues
        Set<String> clues = new HashSet<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT clue_id FROM case_clues WHERE case_id = ?")) {
            ps.setString(1, c.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    clues.add(rs.getString("clue_id"));
                }
            }
        }
        c.setClueIds(clues);
    }

    private Case mapResultSetToCase(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String title = rs.getString("title");
        String desc = rs.getString("description");
        String statusStr = rs.getString("status");
        String priorityStr = rs.getString("priority");
        String lead = rs.getString("lead_investigator");
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");

        CaseStatus status = CaseStatus.OPEN;
        if (statusStr != null) {
            try {
                status = CaseStatus.valueOf(statusStr);
            } catch (IllegalArgumentException ignored) {}
        }

        Priority priority = Priority.MEDIUM;
        if (priorityStr != null) {
            try {
                priority = Priority.valueOf(priorityStr);
            } catch (IllegalArgumentException ignored) {}
        }

        return new Case(
                id,
                title,
                desc,
                status,
                priority,
                lead,
                toLocalDateTime(createdAt),
                toLocalDateTime(updatedAt),
                null, null, null
        );
    }

    private static Timestamp toTimestamp(LocalDateTime ldt) {
        return ldt != null ? Timestamp.valueOf(ldt) : Timestamp.valueOf(LocalDateTime.now());
    }

    private static LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
    }
}
