package com.mysteryvault.service;

import com.mysteryvault.database.CaseDatabaseManager;
import com.mysteryvault.model.DetectiveRank;
import com.mysteryvault.service.dto.DeductionResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

/**
 * Change 4: Dedicated application service managing transactional case completion,
 * idempotent reward attribution, and player progression persistence.
 */
public class CaseCompletionService {

    private final CaseDatabaseManager dbManager;

    public CaseCompletionService(CaseDatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Represents the outcome of attempting to complete a case.
     */
    public static class CompletionOutcome {
        private final boolean success;
        private final boolean wasAlreadyCompleted;
        private final int xpAwarded;
        private final String newRank;
        private final String errorMessage;

        public CompletionOutcome(boolean success, boolean wasAlreadyCompleted, int xpAwarded, String newRank, String errorMessage) {
            this.success = success;
            this.wasAlreadyCompleted = wasAlreadyCompleted;
            this.xpAwarded = xpAwarded;
            this.newRank = newRank;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() { return success; }
        public boolean wasAlreadyCompleted() { return wasAlreadyCompleted; }
        public int getXpAwarded() { return xpAwarded; }
        public String getNewRank() { return newRank; }
        public String getErrorMessage() { return errorMessage; }
    }

    /**
     * Checks whether the case is currently marked as completed in the catalog.
     */
    public boolean isCaseAlreadyCompleted(String caseId) {
        String normalized = CaseDatabaseManager.normalizeCaseId(caseId);
        try (Connection conn = dbManager.getCatalogConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT is_completed FROM cases_catalog WHERE case_id = ?")) {
            ps.setString(1, normalized);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("is_completed") == 1;
                }
            }
        } catch (SQLException ignored) {
        }
        return false;
    }

    /**
     * Atomically and idempotently completes a case upon a validated correct deduction.
     * Guarantees that XP is awarded exactly once per case.
     */
    public synchronized CompletionOutcome completeCaseIfSolved(String caseId, DeductionResult result) {
        if (result == null || !result.isSolved()) {
            recordAttempt(caseId, false, result != null ? result.getScore() : 0);
            return new CompletionOutcome(false, false, 0, null, "Deduction was not verified as solved.");
        }

        String normalized = CaseDatabaseManager.normalizeCaseId(caseId);

        // 1. Check idempotency: If already completed, do not re-award XP
        if (isCaseAlreadyCompleted(normalized)) {
            return new CompletionOutcome(true, true, 0, getCurrentRank(), null);
        }

        int baseReward = 100;
        int performanceReward = Math.max(0, result.getScore());
        int totalXpEarned = baseReward + performanceReward;

        try {
            // 2. Update Case Database (Transaction 1)
            try (Connection caseConn = dbManager.getCaseConnection(normalized)) {
                caseConn.setAutoCommit(false);
                try {
                    // Update case status to SOLVED
                    try (PreparedStatement ps = caseConn.prepareStatement(
                            "UPDATE case_metadata SET status = 'SOLVED' WHERE case_id = ?")) {
                        ps.setString(1, normalized);
                        ps.executeUpdate();
                    }

                    caseConn.commit();
                } catch (SQLException e) {
                    caseConn.rollback();
                    throw e;
                } finally {
                    caseConn.setAutoCommit(true);
                }
            }

            // 3. Update Catalog Database (Transaction 2)
            int updatedXp = 0;
            try (Connection catConn = dbManager.getCatalogConnection()) {
                catConn.setAutoCommit(false);
                try {
                    // Update cases_catalog is_completed = 1
                    try (PreparedStatement ps = catConn.prepareStatement(
                            "UPDATE cases_catalog SET is_completed = 1 WHERE case_id = ?")) {
                        ps.setString(1, normalized);
                        ps.executeUpdate();
                    }

                    // Update player_profile with new XP, completed cases count, deductions, and best score
                    try (PreparedStatement ps = catConn.prepareStatement(
                            "UPDATE player_profile SET " +
                            "experience_points = experience_points + ?, " +
                            "completed_cases = completed_cases + 1, " +
                            "correct_deductions = correct_deductions + 1, " +
                            "total_deductions = total_deductions + 1, " +
                            "best_score = MAX(best_score, ?) " +
                            "WHERE profile_id = 'default_detective'")) {
                        ps.setInt(1, totalXpEarned);
                        ps.setInt(2, result.getScore());
                        ps.executeUpdate();
                    }

                    // Read current total XP to calculate new rank
                    try (PreparedStatement ps = catConn.prepareStatement(
                            "SELECT experience_points FROM player_profile WHERE profile_id = 'default_detective'")) {
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                updatedXp = rs.getInt("experience_points");
                            }
                        }
                    }

                    catConn.commit();
                } catch (SQLException e) {
                    catConn.rollback();
                    throw e;
                } finally {
                    catConn.setAutoCommit(true);
                }
            }

            String newRank = DetectiveRank.getRankTitle(updatedXp);
            return new CompletionOutcome(true, false, totalXpEarned, newRank, null);
        } catch (SQLException e) {
            return new CompletionOutcome(false, false, 0, null, "Database persistence failure: " + e.getMessage());
        }
    }

    /**
     * Records an incorrect deduction attempt: increments total_deductions without awarding XP.
     */
    public synchronized void recordAttempt(String caseId, boolean isCorrect, int score) {
        if (isCorrect) return; // Handled by completeCaseIfSolved
        try (Connection catConn = dbManager.getCatalogConnection()) {
            try (PreparedStatement ps = catConn.prepareStatement(
                    "UPDATE player_profile SET total_deductions = total_deductions + 1 WHERE profile_id = 'default_detective'")) {
                ps.executeUpdate();
            }
        } catch (SQLException ignored) {
        }
    }

    private String getCurrentRank() {
        try (Connection catConn = dbManager.getCatalogConnection();
             PreparedStatement ps = catConn.prepareStatement("SELECT experience_points FROM player_profile WHERE profile_id = 'default_detective'")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return DetectiveRank.getRankTitle(rs.getInt("experience_points"));
                }
            }
        } catch (SQLException ignored) {
        }
        return DetectiveRank.ROOKIE.getTitle();
    }
}
