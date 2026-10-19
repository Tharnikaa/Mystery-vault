package com.mysteryvault.ui.swing.screens;

import com.mysteryvault.model.Connection;
import com.mysteryvault.ui.swing.mock.MockConnectionViewPort;
import com.mysteryvault.ui.swing.viewmodel.GraphNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 test suite verifying Module 4 graph interaction in ConnectionPanel.
 *
 * Verifies:
 * 1. Node selection callback updates displayed entity details and connected relationships.
 * 2. Graph node selection NEVER mutates underlying case data, nodes, or connection models.
 */
public class ConnectionPanelTest {

    private MockConnectionViewPort connectionPort;
    private ConnectionPanel connectionPanel;

    @BeforeAll
    static void initHeadlessEnvironment() {
        System.setProperty("java.awt.headless", "true");
    }

    @BeforeEach
    void setUp() {
        connectionPort = new MockConnectionViewPort();
        connectionPanel = new ConnectionPanel(connectionPort);
    }

    @Test
    @DisplayName("Module 4: Node selection updates connection screen details and relationships without data mutation")
    void testNodeSelectionUpdatesDetailsWithoutMutatingData() {
        // Capture baseline underlying data
        List<GraphNode> baselineNodes = connectionPort.getGraphNodes("CASE_103");
        List<Connection> baselineConnections = connectionPort.getConnections("CASE_103");
        List<String> baselineSummaries = connectionPort.getDiscoveredConnectionSummaries("CASE_103");

        int initialNodeCount = baselineNodes.size();
        int initialConnectionCount = baselineConnections.size();

        // 1. Select MEERA node
        GraphNode meeraNode = null;
        for (GraphNode n : baselineNodes) {
            if ("MEERA".equalsIgnoreCase(n.getId())) {
                meeraNode = n;
                break;
            }
        }
        assertNotNull(meeraNode, "MEERA node should exist in graph nodes");

        connectionPanel.selectNode(meeraNode);

        // Verify UI updated
        assertEquals("MEERA", connectionPanel.getSelectedNodeNameText());
        assertTrue(connectionPanel.getSelectedNodeTypeBadgeText().contains("SUSPECT"));

        List<String> meeraRows = connectionPanel.getDisplayedConnectionRows();
        assertFalse(meeraRows.isEmpty(), "Connection rows should display relationships for Meera");
        boolean hasAudit = meeraRows.stream().anyMatch(r -> r.contains("FINANCIAL AUDIT"));
        boolean hasFingerprint = meeraRows.stream().anyMatch(r -> r.contains("FINGERPRINT MATCH"));
        assertTrue(hasAudit, "Should show financial audit relationship: " + meeraRows);
        assertTrue(hasFingerprint, "Should show fingerprint match relationship: " + meeraRows);

        // 2. Select E01 node
        GraphNode e01Node = null;
        for (GraphNode n : baselineNodes) {
            if ("E01".equalsIgnoreCase(n.getId())) {
                e01Node = n;
                break;
            }
        }
        assertNotNull(e01Node, "E01 node should exist in graph nodes");

        connectionPanel.selectNode(e01Node);

        // Verify UI updated to E01
        assertEquals("E01", connectionPanel.getSelectedNodeNameText());
        assertTrue(connectionPanel.getSelectedNodeTypeBadgeText().contains("EVIDENCE"));
        List<String> e01Rows = connectionPanel.getDisplayedConnectionRows();
        assertFalse(e01Rows.isEmpty(), "Connection rows should display relationships for E01");
        boolean hasDrankFrom = e01Rows.stream().anyMatch(r -> r.contains("DRANK FROM"));
        assertTrue(hasDrankFrom, "Should show DRANK FROM relationship for E01: " + e01Rows);

        // 3. CRITICAL CHECK: Verify underlying case data was NOT mutated
        List<GraphNode> currentNodes = connectionPort.getGraphNodes("CASE_103");
        List<Connection> currentConnections = connectionPort.getConnections("CASE_103");
        List<String> currentSummaries = connectionPort.getDiscoveredConnectionSummaries("CASE_103");

        assertEquals(initialNodeCount, currentNodes.size(), "Node count in connectionPort must remain unchanged");
        assertEquals(initialConnectionCount, currentConnections.size(), "Connection count in connectionPort must remain unchanged");
        assertEquals(baselineSummaries.size(), currentSummaries.size(), "Summaries count must remain unchanged");

        for (int i = 0; i < baselineConnections.size(); i++) {
            assertEquals(baselineConnections.get(i).getId(), currentConnections.get(i).getId());
            assertEquals(baselineConnections.get(i).getRelationType(), currentConnections.get(i).getRelationType());
            assertEquals(baselineConnections.get(i).isStrongLead(), currentConnections.get(i).isStrongLead());
        }
    }

    /**
     * Standalone main runner for direct CLI execution without Maven.
     */
    public static void main(String[] args) {
        initHeadlessEnvironment();
        ConnectionPanelTest test = new ConnectionPanelTest();
        test.setUp();
        test.testNodeSelectionUpdatesDetailsWithoutMutatingData();
        System.out.println("ConnectionPanelTest: ALL SCENARIOS PASSED!");
    }
}
