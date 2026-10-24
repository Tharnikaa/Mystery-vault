package com.mysteryvault.ui.swing.adapters;

import com.mysteryvault.database.CaseDatabaseManager;
import com.mysteryvault.ui.swing.ports.SuspectViewPort;
import com.mysteryvault.ui.swing.viewmodel.SuspectViewModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Production implementation of SuspectViewPort querying the active case database.
 */
public class IntegratedSuspectAdapter implements SuspectViewPort {

    private final CaseDatabaseManager dbManager;

    public IntegratedSuspectAdapter(CaseDatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public List<SuspectViewModel> getSuspectsForCase(String caseId) {
        String effectiveCaseId = resolveCaseId(caseId);
        List<SuspectViewModel> suspects = new ArrayList<>();

        String query = "SELECT suspect_id, name, role, occupation, age, alibi_statement, suspicion_level, is_prime_suspect FROM suspects ORDER BY suspect_id";
        try (Connection conn = dbManager.getCaseConnection(effectiveCaseId);
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            boolean first = true;
            while (rs.next()) {
                String id = rs.getString("suspect_id");
                String name = rs.getString("name");
                String role = rs.getString("role");
                String occupation = rs.getString("occupation");
                int age = rs.getInt("age");
                String alibi = rs.getString("alibi_statement");
                int suspicion = rs.getInt("suspicion_level");
                int trust = Math.max(10, 100 - suspicion);
                boolean prime = rs.getInt("is_prime_suspect") == 1;

                String imgKey = getImageKeyForSuspect(name);
                suspects.add(new SuspectViewModel(
                        id,
                        name,
                        role,
                        occupation,
                        age,
                        prime ? "Prime Suspect" : "Person of Interest",
                        alibi,
                        "Scene Grounds",
                        "Under Surveillance",
                        suspicion,
                        trust,
                        imgKey,
                        first
                ));
                first = false;
            }
        } catch (SQLException e) {
            // Fallback
            suspects.add(new SuspectViewModel("SUS_DEFAULT", "Unknown Suspect", "Person of Interest", 35,
                    "Questioned", "Scene", "None", 50, 50, "suspect_arjun.jpg", true));
        }
        return suspects;
    }

    @Override
    public SuspectViewModel getSuspectById(String suspectId) {
        if (suspectId == null) return null;
        for (SuspectViewModel vm : getSuspectsForCase(dbManager.getActiveCaseId())) {
            if (vm.getId().equalsIgnoreCase(suspectId) || vm.getName().equalsIgnoreCase(suspectId)) {
                return vm;
            }
        }
        return new SuspectViewModel(suspectId, "Suspect " + suspectId, "Person of Interest", 30,
                "Questioned", "Scene", "None", 40, 60, "suspect_arjun.jpg", true);
    }

    private String resolveCaseId(String caseId) {
        if (caseId != null && !caseId.trim().isEmpty()) {
            return caseId;
        }
        String active = dbManager.getActiveCaseId();
        return active != null ? active : "CASE_101";
    }

    private String getImageKeyForSuspect(String name) {
        if (name == null) return "suspect_arjun.jpg";
        String lower = name.toLowerCase();
        if (lower.contains("meera") || lower.contains("evelyn") || lower.contains("elena") || lower.contains("maya")) {
            return "suspect_meera.png";
        } else if (lower.contains("rohan") || lower.contains("marcus") || lower.contains("damien") || lower.contains("kira")) {
            return "suspect_rohan.png";
        } else {
            return "suspect_arjun.jpg";
        }
    }
}
