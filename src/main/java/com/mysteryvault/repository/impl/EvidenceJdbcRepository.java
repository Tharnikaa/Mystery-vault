package com.mysteryvault.repository.impl;

import com.mysteryvault.config.DatabaseConnectionFactory;
import com.mysteryvault.exception.DataAccessException;
import com.mysteryvault.model.DigitalEvidence;
import com.mysteryvault.model.DocumentEvidence;
import com.mysteryvault.model.Evidence;
import com.mysteryvault.repository.EvidenceRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Production JDBC repository for polymorphic Evidence entities (Module 3).
 * Persists and reconstructs DocumentEvidence and DigitalEvidence accurately.
 */
public class EvidenceJdbcRepository implements EvidenceRepository {

    private final DatabaseConnectionFactory connectionFactory;

    public EvidenceJdbcRepository(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = Objects.requireNonNull(connectionFactory, "DatabaseConnectionFactory cannot be null");
    }

    @Override
    public Evidence save(Evidence entity) {
        Objects.requireNonNull(entity, "Evidence entity cannot be null");
        Objects.requireNonNull(entity.getId(), "Evidence ID cannot be null");
        String id = entity.getId().trim();
        if (id.isEmpty()) {
            throw new IllegalArgumentException("Evidence ID cannot be empty");
        }

        try (Connection conn = connectionFactory.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (existsInternal(conn, id)) {
                    updateInternal(conn, entity);
                } else {
                    insertInternal(conn, entity);
                }

                // Ensure link in case_evidence
                if (entity.getCaseId() != null && !entity.getCaseId().trim().isEmpty()) {
                    linkInternal(conn, entity.getCaseId().trim(), id);
                }

                conn.commit();
                return findByIdInternal(conn, id).orElse(entity);
            } catch (SQLException e) {
                conn.rollback();
                throw new DataAccessException("Failed to persist evidence '" + id + "': " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database connection error saving evidence '" + id + "': " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Evidence> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        try (Connection conn = connectionFactory.getConnection()) {
            return findByIdInternal(conn, id.trim());
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find evidence by ID '" + id + "': " + e.getMessage(), e);
        }
    }

    @Override
    public List<Evidence> findAll() {
        String sql = "SELECT * FROM evidence ORDER BY id ASC";
        List<Evidence> list = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToEvidence(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to list all evidence: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Evidence> findByCaseId(String caseId) {
        if (caseId == null || caseId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String sql = "SELECT * FROM evidence WHERE case_id = ? ORDER BY id ASC";
        List<Evidence> list = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, caseId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToEvidence(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to list evidence for case '" + caseId + "': " + e.getMessage(), e);
        }
    }

    @Override
    public List<Evidence> findDiscoveredByCaseId(String caseId) {
        if (caseId == null || caseId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String sql = "SELECT * FROM evidence WHERE case_id = ? AND discovered_in_session = TRUE ORDER BY id ASC";
        List<Evidence> list = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, caseId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToEvidence(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to list discovered evidence for case '" + caseId + "': " + e.getMessage(), e);
        }
    }

    @Override
    public void markDiscovered(String evidenceId, boolean discovered) {
        Objects.requireNonNull(evidenceId, "Evidence ID cannot be null");
        String sql = "UPDATE evidence SET discovered_in_session = ? WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, discovered);
            ps.setString(2, evidenceId.trim());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update discovery status for evidence '" + evidenceId + "': " + e.getMessage(), e);
        }
    }

    @Override
    public void linkEvidenceToCase(String caseId, String evidenceId) {
        Objects.requireNonNull(caseId, "Case ID cannot be null");
        Objects.requireNonNull(evidenceId, "Evidence ID cannot be null");
        try (Connection conn = connectionFactory.getConnection()) {
            linkInternal(conn, caseId.trim(), evidenceId.trim());
        } catch (SQLException e) {
            throw new DataAccessException("Failed to link evidence '" + evidenceId + "' to case '" + caseId + "': " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        String sql = "DELETE FROM evidence WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete evidence '" + id + "': " + e.getMessage(), e);
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
            throw new DataAccessException("Failed to check existence for evidence '" + id + "': " + e.getMessage(), e);
        }
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM evidence";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0L;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to count evidence: " + e.getMessage(), e);
        }
    }

    private boolean existsInternal(Connection conn, String id) throws SQLException {
        String sql = "SELECT 1 FROM evidence WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void insertInternal(Connection conn, Evidence e) throws SQLException {
        String sql = "INSERT INTO evidence (id, name, case_id, description, discovered_at, location_found, collected_by, " +
                "analyzed, evidence_type, author, page_count, classified, document_type, device_type, file_hash, ip_address, file_size_bytes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            populatePreparedStatement(ps, e);
            ps.executeUpdate();
        }
    }

    private void updateInternal(Connection conn, Evidence e) throws SQLException {
        String sql = "UPDATE evidence SET name = ?, case_id = ?, description = ?, discovered_at = ?, location_found = ?, " +
                "collected_by = ?, analyzed = ?, evidence_type = ?, author = ?, page_count = ?, classified = ?, " +
                "document_type = ?, device_type = ?, file_hash = ?, ip_address = ?, file_size_bytes = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getName());
            ps.setString(2, e.getCaseId());
            ps.setString(3, e.getDescription());
            ps.setTimestamp(4, toTimestamp(e.getDiscoveredAt()));
            ps.setString(5, e.getLocationFound());
            ps.setString(6, e.getCollectedBy());
            ps.setBoolean(7, e.isAnalyzed());

            if (e instanceof DocumentEvidence doc) {
                ps.setString(8, "DOCUMENT");
                ps.setString(9, doc.getAuthor());
                ps.setInt(10, doc.getPageCount());
                ps.setBoolean(11, doc.isClassified());
                ps.setString(12, doc.getDocumentType());
                ps.setString(13, null);
                ps.setString(14, null);
                ps.setString(15, null);
                ps.setLong(16, 0L);
            } else if (e instanceof DigitalEvidence dig) {
                ps.setString(8, "DIGITAL");
                ps.setString(9, null);
                ps.setInt(10, 0);
                ps.setBoolean(11, false);
                ps.setString(12, null);
                ps.setString(13, dig.getDeviceType());
                ps.setString(14, dig.getFileHash());
                ps.setString(15, dig.getIpAddress());
                ps.setLong(16, dig.getFileSizeBytes());
            } else {
                ps.setString(8, "GENERIC");
                ps.setString(9, null);
                ps.setInt(10, 0);
                ps.setBoolean(11, false);
                ps.setString(12, null);
                ps.setString(13, null);
                ps.setString(14, null);
                ps.setString(15, null);
                ps.setLong(16, 0L);
            }
            ps.setString(17, e.getId());
            ps.executeUpdate();
        }
    }

    private void linkInternal(Connection conn, String caseId, String evidenceId) throws SQLException {
        String sql = "INSERT INTO case_evidence (case_id, evidence_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, caseId);
            ps.setString(2, evidenceId);
            try {
                ps.executeUpdate();
            } catch (SQLException ignore) {}
        }
    }

    private void populatePreparedStatement(PreparedStatement ps, Evidence e) throws SQLException {
        ps.setString(1, e.getId());
        ps.setString(2, e.getName());
        ps.setString(3, e.getCaseId());
        ps.setString(4, e.getDescription());
        ps.setTimestamp(5, toTimestamp(e.getDiscoveredAt()));
        ps.setString(6, e.getLocationFound());
        ps.setString(7, e.getCollectedBy());
        ps.setBoolean(8, e.isAnalyzed());

        if (e instanceof DocumentEvidence doc) {
            ps.setString(9, "DOCUMENT");
            ps.setString(10, doc.getAuthor());
            ps.setInt(11, doc.getPageCount());
            ps.setBoolean(12, doc.isClassified());
            ps.setString(13, doc.getDocumentType());
            ps.setString(14, null);
            ps.setString(15, null);
            ps.setString(16, null);
            ps.setLong(17, 0L);
        } else if (e instanceof DigitalEvidence dig) {
            ps.setString(9, "DIGITAL");
            ps.setString(10, null);
            ps.setInt(11, 0);
            ps.setBoolean(12, false);
            ps.setString(13, null);
            ps.setString(14, dig.getDeviceType());
            ps.setString(15, dig.getFileHash());
            ps.setString(16, dig.getIpAddress());
            ps.setLong(17, dig.getFileSizeBytes());
        } else {
            ps.setString(9, "GENERIC");
            ps.setString(10, null);
            ps.setInt(11, 0);
            ps.setBoolean(12, false);
            ps.setString(13, null);
            ps.setString(14, null);
            ps.setString(15, null);
            ps.setString(16, null);
            ps.setLong(17, 0L);
        }
    }

    private Optional<Evidence> findByIdInternal(Connection conn, String id) throws SQLException {
        String sql = "SELECT * FROM evidence WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToEvidence(rs));
                }
                return Optional.empty();
            }
        }
    }

    private Evidence mapResultSetToEvidence(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String name = rs.getString("name");
        String caseId = rs.getString("case_id");
        String desc = rs.getString("description");
        Timestamp discoveredAtTs = rs.getTimestamp("discovered_at");
        LocalDateTime discoveredAt = discoveredAtTs != null ? discoveredAtTs.toLocalDateTime() : LocalDateTime.now();
        String location = rs.getString("location_found");
        String collectedBy = rs.getString("collected_by");
        boolean analyzed = rs.getBoolean("analyzed");
        String evidenceType = rs.getString("evidence_type");

        if ("DOCUMENT".equalsIgnoreCase(evidenceType)) {
            String author = rs.getString("author");
            int pageCount = rs.getInt("page_count");
            boolean classified = rs.getBoolean("classified");
            String docType = rs.getString("document_type");
            return new DocumentEvidence(id, name, caseId, desc, discoveredAt, location, collectedBy, analyzed,
                    author, pageCount, classified, docType);
        } else {
            String deviceType = rs.getString("device_type");
            String fileHash = rs.getString("file_hash");
            String ipAddress = rs.getString("ip_address");
            long sizeBytes = rs.getLong("file_size_bytes");
            return new DigitalEvidence(id, name, caseId, desc, discoveredAt, location, collectedBy, analyzed,
                    deviceType, fileHash, ipAddress, sizeBytes);
        }
    }

    private static Timestamp toTimestamp(LocalDateTime ldt) {
        return ldt != null ? Timestamp.valueOf(ldt) : Timestamp.valueOf(LocalDateTime.now());
    }
}
