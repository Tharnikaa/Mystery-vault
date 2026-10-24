package com.mysteryvault.ui.swing.adapters;

import com.mysteryvault.database.CaseDatabaseManager;
import com.mysteryvault.ui.swing.ports.InterrogationViewPort;
import com.mysteryvault.ui.swing.viewmodel.ChatMessageViewModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Production implementation of InterrogationViewPort querying the active case database.
 */
public class IntegratedInterrogationAdapter implements InterrogationViewPort {

    private final CaseDatabaseManager dbManager;

    public IntegratedInterrogationAdapter(CaseDatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public List<ChatMessageViewModel> getDialogueHistory(String suspectId) {
        List<ChatMessageViewModel> history = new ArrayList<>();
        String caseId = resolveCaseId();
        if (suspectId == null) return history;

        String query = "SELECT question_text, response_text FROM interrogation_dialogues " +
                       "WHERE (suspect_id = ? OR suspect_id IN (SELECT suspect_id FROM suspects WHERE name = ? OR name LIKE ?)) " +
                       "AND is_asked = 1 ORDER BY dialogue_id";
        try (Connection conn = dbManager.getCaseConnection(caseId);
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, suspectId);
            ps.setString(2, suspectId);
            ps.setString(3, "%" + suspectId + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    history.add(new ChatMessageViewModel(true, "Detective", rs.getString("question_text")));
                    history.add(new ChatMessageViewModel(false, "Suspect", rs.getString("response_text")));
                }
            }
        } catch (SQLException ignored) {
        }

        // If no asked history yet, provide the initial opening greeting
        if (history.isEmpty()) {
            history.add(new ChatMessageViewModel(true, "Detective", "Please state your full name and movements during the incident."));
            history.add(new ChatMessageViewModel(false, "Suspect", "I am cooperating fully, Detective. Ask your questions and review the records."));
        }

        return history;
    }

    @Override
    public List<String> getAvailableQuestions(String suspectId) {
        String caseId = resolveCaseId();
        List<String> list = new ArrayList<>();
        if (suspectId == null) return list;

        String query = "SELECT question_text FROM interrogation_dialogues " +
                       "WHERE suspect_id = ? OR suspect_id IN (SELECT suspect_id FROM suspects WHERE name = ? OR name LIKE ?) " +
                       "ORDER BY dialogue_id";
        try (Connection conn = dbManager.getCaseConnection(caseId);
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, suspectId);
            ps.setString(2, suspectId);
            ps.setString(3, "%" + suspectId + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getString("question_text"));
                }
            }
        } catch (SQLException ignored) {
        }
        return list;
    }

    @Override
    public ChatMessageViewModel askQuestion(String suspectId, String question) {
        String caseId = resolveCaseId();
        String response = "I have already told you everything I know about that matter.";
        if (suspectId == null) return new ChatMessageViewModel(false, "Suspect", response);

        // Check if there is a predefined question match in DB
        String query = "SELECT dialogue_id, response_text FROM interrogation_dialogues " +
                       "WHERE (suspect_id = ? OR suspect_id IN (SELECT suspect_id FROM suspects WHERE name = ? OR name LIKE ?)) " +
                       "AND question_text LIKE ?";
        try (Connection conn = dbManager.getCaseConnection(caseId);
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, suspectId);
            ps.setString(2, suspectId);
            ps.setString(3, "%" + suspectId + "%");
            ps.setString(4, "%" + (question != null ? question.trim() : "") + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    response = rs.getString("response_text");
                    String dId = rs.getString("dialogue_id");
                    // Mark as asked
                    try (PreparedStatement update = conn.prepareStatement("UPDATE interrogation_dialogues SET is_asked = 1 WHERE dialogue_id = ?")) {
                        update.setString(1, dId);
                        update.executeUpdate();
                    }
                } else {
                    // Try to get any question for this suspect
                    try (PreparedStatement fallback = conn.prepareStatement(
                            "SELECT dialogue_id, response_text FROM interrogation_dialogues " +
                            "WHERE suspect_id = ? OR suspect_id IN (SELECT suspect_id FROM suspects WHERE name = ? OR name LIKE ?) LIMIT 1")) {
                        fallback.setString(1, suspectId);
                        fallback.setString(2, suspectId);
                        fallback.setString(3, "%" + suspectId + "%");
                        try (ResultSet fRs = fallback.executeQuery()) {
                            if (fRs.next()) {
                                response = fRs.getString("response_text");
                            }
                        }
                    }
                }
            }
        } catch (SQLException ignored) {
        }

        return new ChatMessageViewModel(false, "Suspect", response);
    }

    @Override
    public ChatMessageViewModel showEvidence(String suspectId, String evidenceId) {
        String caseId = resolveCaseId();
        String reaction = "That item has nothing to do with me! You are grasping at straws.";

        try (Connection conn = dbManager.getCaseConnection(caseId);
             PreparedStatement ps = conn.prepareStatement("SELECT name, associated_person_id FROM evidence WHERE evidence_id = ?")) {
            ps.setString(1, evidenceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String evName = rs.getString("name");
                    String associated = rs.getString("associated_person_id");
                    boolean matches = suspectId.equalsIgnoreCase(associated);
                    if (!matches && associated != null) {
                        try (PreparedStatement sps = conn.prepareStatement("SELECT name FROM suspects WHERE suspect_id = ?")) {
                            sps.setString(1, associated);
                            try (ResultSet srs = sps.executeQuery()) {
                                if (srs.next() && srs.getString(1).equalsIgnoreCase(suspectId)) {
                                    matches = true;
                                }
                            }
                        }
                    }
                    if (matches) {
                        reaction = "(Shaken) Where did you find " + evName + "?! That wasn't supposed to be there... I demand to speak with my counsel!";
                    } else {
                        reaction = "I recognize " + evName + ", but it belongs to someone else in the facility, not me.";
                    }
                }
            }
        } catch (SQLException ignored) {
        }

        return new ChatMessageViewModel(false, "Suspect", reaction);
    }

    @Override
    public ChatMessageViewModel challengeStatement(String suspectId, String contradictionId) {
        String caseId = resolveCaseId();
        String reaction = "You have caught a discrepancy, but it was merely an honest slip of memory under extreme stress!";

        try (Connection conn = dbManager.getCaseConnection(caseId);
             PreparedStatement ps = conn.prepareStatement("SELECT title, description FROM contradictions WHERE contradiction_id = ?")) {
            ps.setString(1, contradictionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    reaction = "Your confrontation regarding '" + rs.getString("title") + "' leaves the suspect visibly pale. They refuse to answer further without legal representation.";
                }
            }
        } catch (SQLException ignored) {
        }

        return new ChatMessageViewModel(false, "Suspect", reaction);
    }

    private String resolveCaseId() {
        String active = dbManager.getActiveCaseId();
        return active != null ? active : "CASE_101";
    }
}
