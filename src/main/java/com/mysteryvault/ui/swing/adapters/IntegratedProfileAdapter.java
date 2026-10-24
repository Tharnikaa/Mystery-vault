package com.mysteryvault.ui.swing.adapters;

import com.mysteryvault.database.CaseDatabaseManager;
import com.mysteryvault.model.DetectiveRank;
import com.mysteryvault.ui.swing.ports.ProfileViewPort;
import com.mysteryvault.ui.swing.viewmodel.ProfileViewModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Production implementation of ProfileViewPort querying the catalog database.
 * Computes detective rank deterministically from persisted XP via DetectiveRank.
 */
public class IntegratedProfileAdapter implements ProfileViewPort {

    private final CaseDatabaseManager dbManager;

    public IntegratedProfileAdapter(CaseDatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public ProfileViewModel getPlayerProfile() {
        int xp = 0;
        int casesSolved = 0;
        int totalCases = 4;
        int accuracy = 0;
        int casesAttempted = 0;
        int bestScore = 0;

        String query = "SELECT experience_points, completed_cases, correct_deductions, total_deductions, best_score FROM player_profile WHERE profile_id = 'default_detective'";
        try (Connection conn = dbManager.getCatalogConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                xp = rs.getInt("experience_points");
                casesSolved = rs.getInt("completed_cases");
                int correct = rs.getInt("correct_deductions");
                int totalDed = rs.getInt("total_deductions");
                bestScore = rs.getInt("best_score");
                casesAttempted = Math.max(casesSolved, totalDed);

                if (totalDed > 0) {
                    accuracy = (correct * 100) / totalDed;
                }
            }
        } catch (SQLException ignored) {
        }

        String rank = DetectiveRank.getRankTitle(xp);
        return new ProfileViewModel(rank, xp, casesSolved, totalCases, accuracy, casesAttempted, bestScore);
    }
}
