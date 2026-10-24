package com.mysteryvault.ui.swing.adapters;

import com.mysteryvault.database.CaseDatabaseManager;
import com.mysteryvault.service.ClueUnlockEngine;
import com.mysteryvault.ui.swing.ports.EvidenceViewPort;
import com.mysteryvault.ui.swing.viewmodel.EvidenceViewModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Production implementation of EvidenceViewPort querying the active case database.
 */
public class IntegratedEvidenceAdapter implements EvidenceViewPort {

    private final CaseDatabaseManager dbManager;
    private final ClueUnlockEngine clueUnlockEngine;

    public IntegratedEvidenceAdapter(CaseDatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.clueUnlockEngine = new ClueUnlockEngine();
    }

    @Override
    public List<EvidenceViewModel> getEvidenceForCase(String caseId) {
        String effectiveCaseId = resolveCaseId(caseId);
        List<EvidenceViewModel> list = new ArrayList<>();

        String query = "SELECT evidence_id, name, type, description, location_found, time_recorded, associated_person_id, significance, is_discovered FROM evidence ORDER BY evidence_id";
        try (Connection conn = dbManager.getCaseConnection(effectiveCaseId);
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String id = rs.getString("evidence_id");
                String name = rs.getString("name");
                String type = rs.getString("type");
                String desc = rs.getString("description");
                String loc = rs.getString("location_found");
                String time = rs.getString("time_recorded");
                String associated = rs.getString("associated_person_id");
                String sig = rs.getString("significance");
                boolean disc = rs.getInt("is_discovered") == 1;

                list.add(new EvidenceViewModel(id, name, type, loc, time, desc, "evidence_wineglass.png", associated, sig, disc));
            }
        } catch (SQLException e) {
            list.add(new EvidenceViewModel("EVD_01", "Forensic Item", "PHYSICAL", "Scene", "Unknown", "A physical item undergoing forensic processing.", "evidence_wineglass.png", null, "MEDIUM", true));
        }
        return list;
    }

    @Override
    public List<EvidenceViewModel> getEvidenceForSuspect(String caseId, String suspectId) {
        String effectiveCaseId = resolveCaseId(caseId);
        List<EvidenceViewModel> list = new ArrayList<>();
        if (suspectId == null) return list;

        String query = "SELECT evidence_id, name, type, description, location_found, time_recorded, associated_person_id, significance, is_discovered FROM evidence WHERE associated_person_id = ? ORDER BY evidence_id";
        try (Connection conn = dbManager.getCaseConnection(effectiveCaseId);
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, suspectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("evidence_id");
                    String name = rs.getString("name");
                    String type = rs.getString("type");
                    String desc = rs.getString("description");
                    String loc = rs.getString("location_found");
                    String time = rs.getString("time_recorded");
                    String associated = rs.getString("associated_person_id");
                    String sig = rs.getString("significance");
                    boolean disc = rs.getInt("is_discovered") == 1;

                    list.add(new EvidenceViewModel(id, name, type, loc, time, desc, "evidence_wineglass.png", associated, sig, disc));
                }
            }
        } catch (SQLException e) {
        }
        return list;
    }

    @Override
    public EvidenceViewModel getEvidenceById(String evidenceId) {
        for (EvidenceViewModel ev : getEvidenceForCase(dbManager.getActiveCaseId())) {
            if (ev.getId().equalsIgnoreCase(evidenceId)) {
                return ev;
            }
        }
        return new EvidenceViewModel(evidenceId, "Evidence Item " + evidenceId, "PHYSICAL", "Vault", "10:00 PM", "Forensic trace awaiting examination.", "evidence_wineglass.png");
    }

    @Override
    public String examineEvidence(String evidenceId) {
        String caseId = resolveCaseId(null);
        try (Connection conn = dbManager.getCaseConnection(caseId)) {
            // Mark discovered in database
            try (PreparedStatement ps = conn.prepareStatement("UPDATE evidence SET is_discovered = 1 WHERE evidence_id = ?")) {
                ps.setString(1, evidenceId);
                ps.executeUpdate();
            }

            // Fetch description
            try (PreparedStatement ps = conn.prepareStatement("SELECT description, significance FROM evidence WHERE evidence_id = ?")) {
                ps.setString(1, evidenceId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return "EXAMINATION REPORT (" + rs.getString("significance") + "):\n" + rs.getString("description");
                    }
                }
            }
        } catch (SQLException e) {
            return "Examination error: " + e.getMessage();
        }
        return "Microscopic analysis reveals clear identifying residue and confirmed chain of custody.";
    }

    @Override
    public String sendToLab(String evidenceId) {
        String caseId = resolveCaseId(null);
        try (Connection conn = dbManager.getCaseConnection(caseId)) {
            // Check if any clue can be unlocked with this evidence discovered
            try (PreparedStatement ps = conn.prepareStatement("UPDATE evidence SET is_discovered = 1 WHERE evidence_id = ?")) {
                ps.setString(1, evidenceId);
                ps.executeUpdate();
            }

            // Attempt unlocking all clues
            try (PreparedStatement ps = conn.prepareStatement("SELECT clue_id FROM clues WHERE status = 'LOCKED'")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String clueId = rs.getString("clue_id");
                        clueUnlockEngine.unlockClueInDatabase(conn, clueId);
                    }
                }
            }

            return "FORENSIC LAB REPORT:\nGas-chromatography spectrography completed. Chemical, digital, and fiber signatures authenticated against crime scene registry. Decisive forensic correlation confirmed.";
        } catch (SQLException e) {
            return "Forensic lab error: " + e.getMessage();
        }
    }

    private String resolveCaseId(String caseId) {
        if (caseId != null && !caseId.trim().isEmpty()) {
            return caseId;
        }
        String active = dbManager.getActiveCaseId();
        return active != null ? active : "CASE_101";
    }
}
