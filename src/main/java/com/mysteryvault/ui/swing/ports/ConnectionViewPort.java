package com.mysteryvault.ui.swing.ports;

import com.mysteryvault.model.Connection;
import com.mysteryvault.ui.swing.viewmodel.GraphNode;
import java.util.List;

/**
 * Presentation port for the Clue Network Graph and connection summaries.
 * Owned by Member 4 (Module 4).
 */
public interface ConnectionViewPort {
    List<GraphNode> getGraphNodes(String caseId);
    List<Connection> getConnections(String caseId);
    List<String> getDiscoveredConnectionSummaries(String caseId);
}
