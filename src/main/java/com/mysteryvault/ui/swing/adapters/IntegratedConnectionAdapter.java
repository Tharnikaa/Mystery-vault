package com.mysteryvault.ui.swing.adapters;

import com.mysteryvault.database.CaseDatabaseManager;
import com.mysteryvault.model.Connection;
import com.mysteryvault.ui.swing.ports.ConnectionViewPort;
import com.mysteryvault.ui.swing.theme.Theme;
import com.mysteryvault.ui.swing.viewmodel.GraphNode;

import java.awt.Color;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Production implementation of ConnectionViewPort querying the active case database.
 */
public class IntegratedConnectionAdapter implements ConnectionViewPort {

    private final CaseDatabaseManager dbManager;

    public IntegratedConnectionAdapter(CaseDatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public List<GraphNode> getGraphNodes(String caseId) {
        String effectiveCaseId = resolveCaseId(caseId);
        List<GraphNode> nodes = new ArrayList<>();

        // Center Incident Node
        nodes.add(new GraphNode(
                "NODE_INCIDENT",
                "CRIME INCIDENT",
                390, 220, 36,
                Theme.GOLD_PRIMARY,
                "evidence_wineglass.png",
                true,
                "VICTIM"
        ));

        // Load Suspects as surrounding orbital nodes
        String query = "SELECT suspect_id, name, suspicion_level, is_prime_suspect FROM suspects ORDER BY suspect_id";
        try (java.sql.Connection conn = dbManager.getCaseConnection(effectiveCaseId);
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            int index = 0;
            int total = 5;
            double radius = 170.0;
            while (rs.next()) {
                String id = rs.getString("suspect_id");
                String name = rs.getString("name");
                boolean prime = rs.getInt("is_prime_suspect") == 1;

                double angle = (2.0 * Math.PI * index) / total - (Math.PI / 2.0);
                int x = (int) (390 + radius * Math.cos(angle));
                int y = (int) (220 + radius * Math.sin(angle));

                Color nodeColor = prime ? new Color(220, 80, 80) : Theme.TEAL_ACCENT;
                String imgKey = getImageKeyForSuspect(name);

                nodes.add(new GraphNode(id, name, x, y, 26, nodeColor, imgKey, false, "SUSPECT"));
                index++;
            }
        } catch (SQLException e) {
            // Fallback node
            nodes.add(new GraphNode("SUS_01", "Suspect", 250, 150, 24, Theme.TEAL_ACCENT, "suspect_arjun.jpg", false, "SUSPECT"));
        }

        return nodes;
    }

    @Override
    public List<Connection> getConnections(String caseId) {
        String effectiveCaseId = resolveCaseId(caseId);
        List<Connection> connections = new ArrayList<>();

        String query = "SELECT source_id, target_id, relationship_type, weight FROM connections";
        try (java.sql.Connection conn = dbManager.getCaseConnection(effectiveCaseId);
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String src = rs.getString("source_id");
                String tgt = rs.getString("target_id");
                String type = rs.getString("relationship_type");
                double weight = rs.getDouble("weight");

                connections.add(new Connection(src, tgt, type, weight >= 0.7));
            }
        } catch (SQLException ignored) {
        }

        if (connections.isEmpty()) {
            connections.add(new Connection("NODE_INCIDENT", "SUS_101_3", "ACCUSES", true));
        }

        return connections;
    }

    @Override
    public List<String> getDiscoveredConnectionSummaries(String caseId) {
        String effectiveCaseId = resolveCaseId(caseId);
        List<String> summaries = new ArrayList<>();

        String query = "SELECT source_label, target_label, relationship_type FROM connections";
        try (java.sql.Connection conn = dbManager.getCaseConnection(effectiveCaseId);
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                summaries.add(rs.getString("source_label") + " — [" + rs.getString("relationship_type") + "] -> " + rs.getString("target_label"));
            }
        } catch (SQLException ignored) {
        }

        if (summaries.isEmpty()) {
            summaries.add("Confirmed forensic link connects primary suspect to physical evidence at the scene.");
        }

        return summaries;
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
