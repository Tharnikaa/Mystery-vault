package com.mysteryvault.ui.swing.adapters;

import com.mysteryvault.database.CaseDatabaseManager;
import com.mysteryvault.ui.swing.ports.CaseViewPort;
import com.mysteryvault.ui.swing.viewmodel.CaseCardViewModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Production implementation of CaseViewPort backed by the SQLite catalog database.
 */
public class IntegratedCaseAdapter implements CaseViewPort {

    private final CaseDatabaseManager dbManager;
    private String activeCaseId = "CASE_101";

    public IntegratedCaseAdapter(CaseDatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.dbManager.setActiveCaseId(activeCaseId);
    }

    @Override
    public List<CaseCardViewModel> getAvailableCases() {
        List<CaseCardViewModel> list = new ArrayList<>();
        try (Connection conn = dbManager.getCatalogConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT case_id, title, description, difficulty, is_completed FROM cases_catalog ORDER BY case_id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String id = rs.getString("case_id");
                String title = rs.getString("title");
                String diff = rs.getString("difficulty");
                int completed = rs.getInt("is_completed");

                String duration = diff + " • " + (completed == 1 ? "100% Solved" : "Open Investigation");
                String badgeText = completed == 1 ? "SOLVED" : (id.equals(activeCaseId) ? "IN PROGRESS" : "LOCKED");
                String badgeType = completed == 1 ? "SOLVED" : (id.equals(activeCaseId) ? "IN_PROGRESS" : "LOCKED");

                String imageKey = getImageKeyForCase(id);
                boolean isDefault = id.equalsIgnoreCase(activeCaseId);

                list.add(new CaseCardViewModel(id, title, duration, badgeText, badgeType, imageKey, isDefault));
            }
        } catch (SQLException e) {
            // Fallback default list if DB error
            list.add(new CaseCardViewModel("CASE_101", "The Missing Diamond", "MEDIUM • Open", "IN PROGRESS", "IN_PROGRESS", "case_diamond.png", true));
            list.add(new CaseCardViewModel("CASE_102", "The Library Code", "HARD • Open", "LOCKED", "LOCKED", "case_library.png", false));
            list.add(new CaseCardViewModel("CASE_103", "The Poisoned Drink", "EASY • Open", "LOCKED", "LOCKED", "case_poison.png", false));
            list.add(new CaseCardViewModel("CASE_104", "The Vanishing Prototype", "HARD • Open", "LOCKED", "LOCKED", "case_prototype.png", false));
        }
        return list;
    }

    @Override
    public CaseCardViewModel getActiveCase() {
        for (CaseCardViewModel vm : getAvailableCases()) {
            if (vm.getId().equalsIgnoreCase(activeCaseId)) {
                return vm;
            }
        }
        return new CaseCardViewModel("CASE_101", "The Missing Diamond", "MEDIUM", "IN PROGRESS", "IN_PROGRESS", "case_diamond.png", true);
    }

    @Override
    public void selectCase(String caseId) {
        if (caseId != null && !caseId.trim().isEmpty()) {
            this.activeCaseId = CaseDatabaseManager.normalizeCaseId(caseId);
            this.dbManager.setActiveCaseId(this.activeCaseId);
        }
    }

    private String getImageKeyForCase(String caseId) {
        switch (caseId.toUpperCase()) {
            case "CASE_101": return "case_diamond.png";
            case "CASE_102": return "case_library.png";
            case "CASE_103": return "case_poison.png";
            case "CASE_104": return "case_prototype.png";
            default: return "case_diamond.png";
        }
    }
}
