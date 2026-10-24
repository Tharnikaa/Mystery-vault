package com.mysteryvault.ui.swing.screens;

import com.mysteryvault.model.Connection;
import com.mysteryvault.ui.swing.components.*;
import com.mysteryvault.ui.swing.mock.MockConnectionViewPort;
import com.mysteryvault.ui.swing.navigation.ScreenManager;
import com.mysteryvault.ui.swing.ports.ConnectionViewPort;
import com.mysteryvault.ui.swing.theme.Theme;
import com.mysteryvault.ui.swing.viewmodel.GraphNode;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Module 13 / Screen 9 — Case Connection Engine Screen.
 *
 * Owned by Member 4.
 * Hosts the CustomGraphCanvas interactive Java 2D clue network and displays
 * selected entity relationships and discovered connection summaries.
 * Graph node selection updates the UI dynamically without mutating underlying case data.
 */
public class ConnectionPanel extends JPanel implements com.mysteryvault.ui.swing.navigation.RefreshableScreen {

    private final ConnectionViewPort connectionPort;
    private final CustomGraphCanvas graphCanvas;

    private JLabel selectedNodeNameLbl;
    private JLabel selectedNodeTypeLbl;
    private JPanel itemsBox;
    private String currentCaseId = "CASE_101";
    private final List<String> currentDisplayedRows = new ArrayList<>();

    public ConnectionPanel(ConnectionViewPort connectionPort) {
        this.connectionPort = connectionPort != null ? connectionPort : new MockConnectionViewPort();
        this.graphCanvas = new CustomGraphCanvas();

        setOpaque(true);
        setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());

        initUI();
    }

    public ConnectionPanel() {
        this(new MockConnectionViewPort());
    }

    @Override
    public void refreshScreen(String caseId) {
        this.currentCaseId = caseId != null ? caseId : "CASE_101";
        List<GraphNode> nodes = connectionPort.getGraphNodes(this.currentCaseId);
        List<Connection> connections = connectionPort.getConnections(this.currentCaseId);
        graphCanvas.setGraph(nodes, connections);
        graphCanvas.repaint();
    }

    private void initUI() {
        ScreenHeader header = new ScreenHeader("9. CASE CONNECTION ENGINE");
        add(header, BorderLayout.NORTH);

        JPanel mainSplit = new JPanel(new BorderLayout(24, 0));
        mainSplit.setOpaque(false);
        mainSplit.setBorder(BorderFactory.createEmptyBorder(10, 40, 20, 40));

        // Load graph data into CustomGraphCanvas
        List<GraphNode> nodes = connectionPort.getGraphNodes(currentCaseId);
        List<Connection> connections = connectionPort.getConnections(currentCaseId);
        graphCanvas.setGraph(nodes, connections);

        mainSplit.add(graphCanvas, BorderLayout.CENTER);

        // Right Sidebar: CONNECTIONS FOUND Card
        GlassPanel rightCard = new GlassPanel(new BorderLayout(0, 14));
        rightCard.setPreferredSize(new Dimension(340, 520));
        rightCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topHeaderBox = new JPanel();
        topHeaderBox.setLayout(new BoxLayout(topHeaderBox, BoxLayout.Y_AXIS));
        topHeaderBox.setOpaque(false);

        JLabel titleLbl = new JLabel("CONNECTIONS FOUND");
        titleLbl.setFont(Theme.titleFont(16));
        titleLbl.setForeground(Theme.TEXT_GOLD);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel entityRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        entityRow.setOpaque(false);
        entityRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        selectedNodeNameLbl = new JLabel("VICTIM");
        selectedNodeNameLbl.setFont(Theme.bold(14));
        selectedNodeNameLbl.setForeground(Theme.TEXT_PRIMARY);

        selectedNodeTypeLbl = new JLabel("• VICTIM");
        selectedNodeTypeLbl.setFont(Theme.regular(12));
        selectedNodeTypeLbl.setForeground(Theme.TEXT_GOLD);

        entityRow.add(selectedNodeNameLbl);
        entityRow.add(selectedNodeTypeLbl);

        topHeaderBox.add(titleLbl);
        topHeaderBox.add(Box.createRigidArea(new Dimension(0, 4)));
        topHeaderBox.add(entityRow);

        itemsBox = new JPanel();
        itemsBox.setLayout(new BoxLayout(itemsBox, BoxLayout.Y_AXIS));
        itemsBox.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(itemsBox);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        CustomButton btnRun = new CustomButton("RUN ANALYSIS", CustomButton.ButtonStyle.GOLD);
        btnRun.setPreferredSize(new Dimension(300, 42));
        btnRun.addActionListener(e -> ScreenManager.getInstance().showScreen(ScreenManager.CASE_ANALYSIS));

        rightCard.add(topHeaderBox, BorderLayout.NORTH);
        rightCard.add(scrollPane, BorderLayout.CENTER);
        rightCard.add(btnRun, BorderLayout.SOUTH);

        mainSplit.add(rightCard, BorderLayout.EAST);
        add(mainSplit, BorderLayout.CENTER);

        // Connect graph node selection callback
        graphCanvas.setNodeSelectionListener(this::onNodeSelected);

        // Initial selection view
        GraphNode initialNode = graphCanvas.getSelectedNode();
        if (initialNode != null) {
            onNodeSelected(initialNode);
        }
    }

    /**
     * Callback triggered when a node is selected on the CustomGraphCanvas.
     * Updates displayed entity details and associated connections.
     * Note: This strictly inspects presentation data and never modifies the underlying case models or ports.
     */
    public void onNodeSelected(GraphNode node) {
        if (node == null) return;

        selectedNodeNameLbl.setText(node.getLabel());
        selectedNodeTypeLbl.setText("• " + (node.getNodeType() != null ? node.getNodeType() : "NODE"));

        itemsBox.removeAll();
        currentDisplayedRows.clear();

        List<Connection> allConnections = connectionPort.getConnections(currentCaseId);
        List<Connection> related = new ArrayList<>();
        for (Connection c : allConnections) {
            if (node.getId().equalsIgnoreCase(c.getSourceId()) || node.getId().equalsIgnoreCase(c.getTargetId())) {
                related.add(c);
            }
        }

        JLabel sectionTitle = new JLabel("CONNECTED RELATIONSHIPS (" + related.size() + "):");
        sectionTitle.setFont(Theme.bold(11));
        sectionTitle.setForeground(Theme.TEXT_MUTED);
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        itemsBox.add(sectionTitle);
        itemsBox.add(Box.createRigidArea(new Dimension(0, 8)));

        if (related.isEmpty()) {
            JLabel emptyLbl = new JLabel("No direct leads connected to this entity.");
            emptyLbl.setFont(Theme.regular(12));
            emptyLbl.setForeground(Theme.TEXT_SECONDARY);
            itemsBox.add(emptyLbl);
        } else {
            for (Connection conn : related) {
                String otherId = node.getId().equalsIgnoreCase(conn.getSourceId()) ? conn.getTargetId() : conn.getSourceId();
                String relText = otherId + " ↔ " + conn.getRelationType().replace('_', ' ') + (conn.isStrongLead() ? " ★" : "");
                currentDisplayedRows.add(relText);

                Color color = conn.isStrongLead() ? Theme.GOLD_PRIMARY : Theme.TEAL_ACCENT;
                itemsBox.add(createConnectionRow(relText, color));
                itemsBox.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        }

        // Also append overall case discoveries for comprehensive investigation context
        itemsBox.add(Box.createRigidArea(new Dimension(0, 12)));
        JLabel summarySectionTitle = new JLabel("DISCOVERED SUMMARIES:");
        summarySectionTitle.setFont(Theme.bold(11));
        summarySectionTitle.setForeground(Theme.TEXT_MUTED);
        summarySectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        itemsBox.add(summarySectionTitle);
        itemsBox.add(Box.createRigidArea(new Dimension(0, 8)));

        List<String> summaries = connectionPort.getDiscoveredConnectionSummaries(currentCaseId);
        for (int i = 0; i < summaries.size(); i++) {
            Color bulletColor = (i == 1) ? Theme.TEAL_ACCENT : (i == 2 ? Theme.GOLD_PRIMARY : (i == 3 ? Theme.RED_CONFLICT : Theme.TEXT_PRIMARY));
            itemsBox.add(createConnectionRow(summaries.get(i), bulletColor));
            if (i < summaries.size() - 1) {
                itemsBox.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        }

        itemsBox.revalidate();
        itemsBox.repaint();
    }

    private JPanel createConnectionRow(String text, Color accentColor) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel bullet = new JLabel("•");
        bullet.setFont(Theme.bold(14));
        bullet.setForeground(accentColor);

        JLabel textLbl = new JLabel(text);
        textLbl.setFont(Theme.regular(12));
        textLbl.setForeground(Theme.TEXT_PRIMARY);

        row.add(bullet);
        row.add(textLbl);
        return row;
    }

    public CustomGraphCanvas getGraphCanvas() {
        return graphCanvas;
    }

    public GraphNode getSelectedNode() {
        return graphCanvas.getSelectedNode();
    }

    public void selectNode(GraphNode node) {
        graphCanvas.setSelectedNode(node);
    }

    public String getSelectedNodeNameText() {
        return selectedNodeNameLbl != null ? selectedNodeNameLbl.getText() : "";
    }

    public String getSelectedNodeTypeBadgeText() {
        return selectedNodeTypeLbl != null ? selectedNodeTypeLbl.getText() : "";
    }

    public List<String> getDisplayedConnectionRows() {
        return Collections.unmodifiableList(currentDisplayedRows);
    }
}
