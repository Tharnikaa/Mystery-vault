package com.mysteryvault.repository.impl;

import com.mysteryvault.config.DatabaseConnectionFactory;
import com.mysteryvault.exception.DataAccessException;
import com.mysteryvault.model.Suspect;
import com.mysteryvault.repository.SuspectRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * JDBC implementation of SuspectRepository (Module 2).
 * Persists person/suspect profiles and manages case-suspect associations.
 */
public class SuspectJdbcRepository implements SuspectRepository {

    private final DatabaseConnectionFactory connectionFactory;

    public SuspectJdbcRepository(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = Objects.requireNonNull(connectionFactory, "DatabaseConnectionFactory cannot be null");
    }

    @Override
    public Suspect save(Suspect entity) {
        Objects.requireNonNull(entity, "Suspect cannot be null");
        Objects.requireNonNull(entity.getId(), "Suspect ID cannot be null");
        String id = entity.getId().trim();
        if (id.isEmpty()) {
            throw new IllegalArgumentException("Suspect ID cannot be empty");
        }

        try (Connection conn = connectionFactory.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (existsInternal(conn, id)) {
                    updateInternal(conn, entity);
                } else {
                    insertInternal(conn, entity);
                }
                conn.commit();
                return findByIdInternal(conn, id).orElse(entity);
            } catch (SQLException e) {
                conn.rollback();
                throw new DataAccessException("Failed to persist suspect '" + id + "': " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database connection error while saving suspect '" + id + "': " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Suspect> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        try (Connection conn = connectionFactory.getConnection()) {
            return findByIdInternal(conn, id.trim());
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find suspect by ID '" + id + "': " + e.getMessage(), e);
        }
    }

    @Override
    public List<Suspect> findAll() {
        String sql = "SELECT id, name, role, notes, occupation, age, alibi, suspicion_score, motive_description, alibi_verified " +
                "FROM suspects ORDER BY name ASC";
        List<Suspect> list = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToSuspect(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to list all suspects: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Suspect> findByCaseId(String caseId) {
        if (caseId == null || caseId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String sql = "SELECT s.id, s.name, s.role, s.notes, s.occupation, s.age, s.alibi, s.suspicion_score, s.motive_description, s.alibi_verified " +
                "FROM suspects s " +
                "JOIN case_suspects cs ON s.id = cs.suspect_id " +
                "WHERE cs.case_id = ? " +
                "ORDER BY s.name ASC";
        List<Suspect> list = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, caseId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToSuspect(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to list suspects for case '" + caseId + "': " + e.getMessage(), e);
        }
    }

    @Override
    public List<Suspect> searchByName(String nameQuery) {
        if (nameQuery == null || nameQuery.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String sql = "SELECT id, name, role, notes, occupation, age, alibi, suspicion_score, motive_description, alibi_verified " +
                "FROM suspects WHERE LOWER(name) LIKE ? ORDER BY name ASC";
        List<Suspect> list = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + nameQuery.trim().toLowerCase() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToSuspect(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to search suspects by name '" + nameQuery + "': " + e.getMessage(), e);
        }
    }

    @Override
    public void linkSuspectToCase(String caseId, String suspectId) {
        Objects.requireNonNull(caseId, "Case ID cannot be null");
        Objects.requireNonNull(suspectId, "Suspect ID cannot be null");

        String sql = "INSERT INTO case_suspects (case_id, suspect_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, caseId.trim());
            ps.setString(2, suspectId.trim());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed linking suspect '" + suspectId + "' to case '" + caseId + "': " + e.getMessage(), e);
        }
    }

    @Override
    public void unlinkSuspectFromCase(String caseId, String suspectId) {
        if (caseId == null || suspectId == null) return;
        String sql = "DELETE FROM case_suspects WHERE case_id = ? AND suspect_id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, caseId.trim());
            ps.setString(2, suspectId.trim());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed unlinking suspect '" + suspectId + "' from case '" + caseId + "': " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        String sql = "DELETE FROM suspects WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete suspect '" + id + "': " + e.getMessage(), e);
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
            throw new DataAccessException("Failed to check existence for suspect '" + id + "': " + e.getMessage(), e);
        }
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM suspects";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0L;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to count suspects: " + e.getMessage(), e);
        }
    }

    private boolean existsInternal(Connection conn, String id) throws SQLException {
        String sql = "SELECT 1 FROM suspects WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void insertInternal(Connection conn, Suspect entity) throws SQLException {
        String sql = "INSERT INTO suspects (id, name, role, notes, occupation, age, alibi, suspicion_score, motive_description, alibi_verified) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getId());
            ps.setString(2, entity.getName());
            ps.setString(3, entity.getRole());
            ps.setString(4, entity.getNotes());
            ps.setString(5, entity.getOccupation());
            ps.setInt(6, entity.getAge());
            ps.setString(7, entity.getAlibi());
            ps.setInt(8, entity.getSuspicionScore());
            ps.setString(9, entity.getMotiveDescription());
            ps.setBoolean(10, entity.isAlibiVerified());
            ps.executeUpdate();
        }
    }

    private void updateInternal(Connection conn, Suspect entity) throws SQLException {
        String sql = "UPDATE suspects SET name = ?, role = ?, notes = ?, occupation = ?, age = ?, alibi = ?, " +
                "suspicion_score = ?, motive_description = ?, alibi_verified = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getName());
            ps.setString(2, entity.getRole());
            ps.setString(3, entity.getNotes());
            ps.setString(4, entity.getOccupation());
            ps.setInt(5, entity.getAge());
            ps.setString(6, entity.getAlibi());
            ps.setInt(7, entity.getSuspicionScore());
            ps.setString(8, entity.getMotiveDescription());
            ps.setBoolean(9, entity.isAlibiVerified());
            ps.setString(10, entity.getId());
            ps.executeUpdate();
        }
    }

    private Optional<Suspect> findByIdInternal(Connection conn, String id) throws SQLException {
        String sql = "SELECT id, name, role, notes, occupation, age, alibi, suspicion_score, motive_description, alibi_verified " +
                "FROM suspects WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSuspect(rs));
                }
                return Optional.empty();
            }
        }
    }

    private Suspect mapResultSetToSuspect(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String name = rs.getString("name");
        String role = rs.getString("role");
        String notes = rs.getString("notes");
        String occupation = rs.getString("occupation");
        int age = rs.getInt("age");
        String alibi = rs.getString("alibi");
        int suspicionScore = rs.getInt("suspicion_score");
        String motive = rs.getString("motive_description");
        boolean alibiVerified = rs.getBoolean("alibi_verified");

        Suspect s = new Suspect(id, name, occupation, age, alibi, suspicionScore, motive, alibiVerified);
        s.setRole(role);
        s.setNotes(notes);
        return s;
    }
}
