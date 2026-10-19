package com.mysteryvault.repository.impl;

import com.mysteryvault.config.DatabaseConnectionFactory;
import com.mysteryvault.exception.DataAccessException;
import com.mysteryvault.model.Clue;
import com.mysteryvault.repository.ClueRepository;

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
 * Production JDBC repository for Clue entities and prerequisite unlocking logic (Module 3).
 */
public class ClueJdbcRepository implements ClueRepository {

    private final DatabaseConnectionFactory connectionFactory;

    public ClueJdbcRepository(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = Objects.requireNonNull(connectionFactory, "DatabaseConnectionFactory cannot be null");
    }

    @Override
    public Clue save(Clue entity) {
        Objects.requireNonNull(entity, "Clue entity cannot be null");
        Objects.requireNonNull(entity.getId(), "Clue ID cannot be null");
        String id = entity.getId().trim();
        if (id.isEmpty()) {
            throw new IllegalArgumentException("Clue ID cannot be empty");
        }

        try (Connection conn = connectionFactory.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (existsInternal(conn, id)) {
                    updateInternal(conn, entity);
                } else {
                    insertInternal(conn, entity);
                }

                savePrerequisitesInternal(conn, id, entity.getRequiredEvidenceIds());

                if (entity.getCaseId() != null && !entity.getCaseId().trim().isEmpty()) {
                    linkInternal(conn, entity.getCaseId().trim(), id);
                }

                conn.commit();
                return findByIdInternal(conn, id).orElse(entity);
            } catch (SQLException e) {
                conn.rollback();
                throw new DataAccessException("Failed to persist clue '" + id + "': " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database connection error while saving clue '" + id + "': " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Clue> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        try (Connection conn = connectionFactory.getConnection()) {
            return findByIdInternal(conn, id.trim());
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find clue by ID '" + id + "': " + e.getMessage(), e);
        }
    }

    @Override
    public List<Clue> findAll() {
        String sql = "SELECT * FROM clues ORDER BY id ASC";
        List<Clue> list = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Clue c = mapResultSetToClue(rs);
                c.setRequiredEvidenceIds(findPrerequisitesInternal(conn, c.getId()));
                list.add(c);
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to list all clues: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Clue> findByCaseId(String caseId) {
        if (caseId == null || caseId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String sql = "SELECT * FROM clues WHERE case_id = ? ORDER BY id ASC";
        List<Clue> list = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, caseId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Clue c = mapResultSetToClue(rs);
                    c.setRequiredEvidenceIds(findPrerequisitesInternal(conn, c.getId()));
                    list.add(c);
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to list clues for case '" + caseId + "': " + e.getMessage(), e);
        }
    }

    @Override
    public List<Clue> findUnlockedByCaseId(String caseId) {
        if (caseId == null || caseId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String sql = "SELECT * FROM clues WHERE case_id = ? AND unlocked = TRUE ORDER BY id ASC";
        List<Clue> list = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, caseId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Clue c = mapResultSetToClue(rs);
                    c.setRequiredEvidenceIds(findPrerequisitesInternal(conn, c.getId()));
                    list.add(c);
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to list unlocked clues for case '" + caseId + "': " + e.getMessage(), e);
        }
    }

    @Override
    public void savePrerequisites(String clueId, Set<String> requiredEvidenceIds) {
        Objects.requireNonNull(clueId, "Clue ID cannot be null");
        try (Connection conn = connectionFactory.getConnection()) {
            conn.setAutoCommit(false);
            try {
                savePrerequisitesInternal(conn, clueId.trim(), requiredEvidenceIds);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new DataAccessException("Failed saving prerequisites for clue '" + clueId + "': " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database connection error while saving prerequisites: " + e.getMessage(), e);
        }
    }

    @Override
    public Set<String> findPrerequisites(String clueId) {
        if (clueId == null || clueId.trim().isEmpty()) {
            return Collections.emptySet();
        }
        try (Connection conn = connectionFactory.getConnection()) {
            return findPrerequisitesInternal(conn, clueId.trim());
        } catch (SQLException e) {
            throw new DataAccessException("Failed to retrieve prerequisites for clue '" + clueId + "': " + e.getMessage(), e);
        }
    }

    @Override
    public void updateUnlockedState(String clueId, boolean unlocked, LocalDateTime unlockedAt) {
        Objects.requireNonNull(clueId, "Clue ID cannot be null");
        String sql = "UPDATE clues SET unlocked = ?, unlocked_at = ? WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, unlocked);
            ps.setTimestamp(2, unlockedAt != null ? Timestamp.valueOf(unlockedAt) : null);
            ps.setString(3, clueId.trim());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed updating unlocked state for clue '" + clueId + "': " + e.getMessage(), e);
        }
    }

    @Override
    public void linkClueToCase(String caseId, String clueId) {
        Objects.requireNonNull(caseId, "Case ID cannot be null");
        Objects.requireNonNull(clueId, "Clue ID cannot be null");
        try (Connection conn = connectionFactory.getConnection()) {
            linkInternal(conn, caseId.trim(), clueId.trim());
        } catch (SQLException e) {
            throw new DataAccessException("Failed linking clue '" + clueId + "' to case '" + caseId + "': " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        String sql = "DELETE FROM clues WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete clue '" + id + "': " + e.getMessage(), e);
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
            throw new DataAccessException("Failed to check existence for clue '" + id + "': " + e.getMessage(), e);
        }
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM clues";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0L;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to count clues: " + e.getMessage(), e);
        }
    }

    private boolean existsInternal(Connection conn, String id) throws SQLException {
        String sql = "SELECT 1 FROM clues WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void insertInternal(Connection conn, Clue entity) throws SQLException {
        String sql = "INSERT INTO clues (id, case_id, title, description, unlocked, unlocked_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getId());
            ps.setString(2, entity.getCaseId());
            ps.setString(3, entity.getTitle());
            ps.setString(4, entity.getDescription());
            ps.setBoolean(5, entity.isUnlocked());
            ps.setTimestamp(6, entity.getUnlockedAt() != null ? Timestamp.valueOf(entity.getUnlockedAt()) : null);
            ps.executeUpdate();
        }
    }

    private void updateInternal(Connection conn, Clue entity) throws SQLException {
        String sql = "UPDATE clues SET case_id = ?, title = ?, description = ?, unlocked = ?, unlocked_at = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getCaseId());
            ps.setString(2, entity.getTitle());
            ps.setString(3, entity.getDescription());
            ps.setBoolean(4, entity.isUnlocked());
            ps.setTimestamp(5, entity.getUnlockedAt() != null ? Timestamp.valueOf(entity.getUnlockedAt()) : null);
            ps.setString(6, entity.getId());
            ps.executeUpdate();
        }
    }

    private void savePrerequisitesInternal(Connection conn, String clueId, Set<String> requiredEvidenceIds) throws SQLException {
        try (PreparedStatement del = conn.prepareStatement("DELETE FROM clue_prerequisites WHERE clue_id = ?")) {
            del.setString(1, clueId);
            del.executeUpdate();
        }

        if (requiredEvidenceIds != null && !requiredEvidenceIds.isEmpty()) {
            String insSql = "INSERT INTO clue_prerequisites (clue_id, required_evidence_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
            try (PreparedStatement ins = conn.prepareStatement(insSql)) {
                for (String evId : requiredEvidenceIds) {
                    if (evId != null && !evId.trim().isEmpty()) {
                        ins.setString(1, clueId);
                        ins.setString(2, evId.trim());
                        try {
                            ins.executeUpdate();
                        } catch (SQLException ignore) {}
                    }
                }
            }
        }
    }

    private Set<String> findPrerequisitesInternal(Connection conn, String clueId) throws SQLException {
        Set<String> set = new HashSet<>();
        String sql = "SELECT required_evidence_id FROM clue_prerequisites WHERE clue_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, clueId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    set.add(rs.getString("required_evidence_id"));
                }
            }
        }
        return set;
    }

    private void linkInternal(Connection conn, String caseId, String clueId) throws SQLException {
        String sql = "INSERT INTO case_clues (case_id, clue_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, caseId);
            ps.setString(2, clueId);
            try {
                ps.executeUpdate();
            } catch (SQLException ignore) {}
        }
    }

    private Optional<Clue> findByIdInternal(Connection conn, String id) throws SQLException {
        String sql = "SELECT * FROM clues WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Clue c = mapResultSetToClue(rs);
                    c.setRequiredEvidenceIds(findPrerequisitesInternal(conn, id));
                    return Optional.of(c);
                }
                return Optional.empty();
            }
        }
    }

    private Clue mapResultSetToClue(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String caseId = rs.getString("case_id");
        String title = rs.getString("title");
        String desc = rs.getString("description");
        boolean unlocked = rs.getBoolean("unlocked");
        Timestamp unlockedAtTs = rs.getTimestamp("unlocked_at");

        Clue c = new Clue(id, caseId, title, desc);
        c.setUnlocked(unlocked);
        if (unlockedAtTs != null) {
            c.setUnlockedAt(unlockedAtTs.toLocalDateTime());
        }
        return c;
    }
}
