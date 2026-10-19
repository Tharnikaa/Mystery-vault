package com.mysteryvault.ui.swing.adapters;

import com.mysteryvault.model.Connection;
import com.mysteryvault.ui.swing.ports.ConnectionViewPort;
import com.mysteryvault.ui.swing.theme.Theme;
import com.mysteryvault.ui.swing.viewmodel.GraphNode;

import java.awt.Color;
import java.util.*;

/**
 * Production presentation adapter connecting ConnectionViewPort to Member 4's Module 4
 * Connection model and CustomGraphCanvas renderer.
 */
public class IntegratedConnectionAdapter implements ConnectionViewPort {

    private final List<GraphNode> nodes = new ArrayList<>();
    private final List<Connection> connections = new ArrayList<>();
    private final List<String> connectionSummaries = new ArrayList<>();

    public IntegratedConnectionAdapter() {
        initDefaultGraph();
    }

    private void initDefaultGraph() {
        // Center victim node
        nodes.add(new GraphNode("VICTIM", "VICTIM", 270, 220, 26, Color.WHITE, null, true, "VICTIM"));

        // Surrounding suspect and evidence nodes
        nodes.add(new GraphNode("ARJUN", "ARJUN", 270, 70, 22, Theme.TEXT_SECONDARY, "suspect_arjun.jpg", false, "SUSPECT"));
        nodes.add(new GraphNode("MEERA", "MEERA", 420, 110, 24, Theme.GOLD_PRIMARY, "suspect_meera.png", false, "SUSPECT"));
        nodes.add(new GraphNode("ROHAN", "ROHAN", 440, 260, 22, Theme.TEXT_SECONDARY, "suspect_rohan.png", false, "SUSPECT"));
        nodes.add(new GraphNode("E04", "E04", 390, 370, 20, Theme.TEAL_ACCENT, null, false, "EVIDENCE"));
        nodes.add(new GraphNode("TIMELINE", "TIMELINE", 270, 390, 20, Theme.TEXT_SECONDARY, null, false, "TIMELINE"));
        nodes.add(new GraphNode("E03", "E03", 150, 360, 20, Theme.TEAL_ACCENT, null, false, "EVIDENCE"));
        nodes.add(new GraphNode("E01", "E01", 100, 220, 20, Theme.GOLD_PRIMARY, null, false, "EVIDENCE"));

        // Graph relationships
        connections.add(new Connection("c1", "VICTIM", "ARJUN", "BUSINESS_PARTNER", false));
        connections.add(new Connection("c2", "VICTIM", "MEERA", "FINANCIAL_AUDIT", true));
        connections.add(new Connection("c3", "VICTIM", "ROHAN", "RESTAURANT_STAFF", false));
        connections.add(new Connection("c4", "VICTIM", "TIMELINE", "COLLAPSE_TIME", false));
        connections.add(new Connection("c5", "VICTIM", "E01", "DRANK_FROM", true));
        connections.add(new Connection("c6", "MEERA", "E01", "FINGERPRINT_MATCH", true));
        connections.add(new Connection("c7", "ARJUN", "MEERA", "DISCUSSED_ACCOUNTS", false));
        connections.add(new Connection("c8", "VICTIM", "E04", "INCOMING_CALL", false));
        connections.add(new Connection("c9", "VICTIM", "E03", "SEATED_BOOTH", false));

        connectionSummaries.addAll(Arrays.asList(
                "Shared Crime Scene – Private East Salon & Sommelier Cellar",
                "Temporal Convergence – 08:42 – 08:45 PM Cellar Access",
                "Physical Link – E01 (Wine Glass) -> Meera / Suspect #2",
                "Statement Contradiction – Cellar denial vs Security Cam #04 log"
        ));
    }

    @Override
    public List<GraphNode> getGraphNodes(String caseId) {
        return Collections.unmodifiableList(new ArrayList<>(nodes));
    }

    @Override
    public List<Connection> getConnections(String caseId) {
        return Collections.unmodifiableList(new ArrayList<>(connections));
    }

    @Override
    public List<String> getDiscoveredConnectionSummaries(String caseId) {
        return Collections.unmodifiableList(new ArrayList<>(connectionSummaries));
    }
}
