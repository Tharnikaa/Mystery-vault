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
import java.util.List;

public class ConnectionPanel extends JPanel {

    private final ConnectionViewPort connectionPort;
    private final CustomGraphCanvas graphCanvas;

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

    private void initUI() {
        ScreenHeader header = new ScreenHeader("9. CASE CONNECTION ENGINE");
        add(header, BorderLayout.NORTH);

        JPanel mainSplit = new JPanel(new BorderLayout(24, 0));
        mainSplit.setOpaque(false);
        mainSplit.setBorder(BorderFactory.createEmptyBorder(10, 40, 20, 40));

        // Load graph data into CustomGraphCanvas
        List<GraphNode> nodes = connectionPort.getGraphNodes("CASE_103");
        List<Connection> connections = connectionPort.getConnections("CASE_103");
        graphCanvas.setGraph(nodes, connections);

        mainSplit.add(graphCanvas, BorderLayout.CENTER);

        // Right Sidebar: CONNECTIONS FOUND Card
        GlassPanel rightCard = new GlassPanel(new BorderLayout(0, 16));
        rightCard.setPreferredSize(new Dimension(320, 520));
        rightCard.setBorder(BorderFactory.createEmptyBorder(22, 20, 22, 20));

        JLabel titleLbl = new JLabel("CONNECTIONS FOUND");
        titleLbl.setFont(Theme.titleFont(16));
        titleLbl.setForeground(Theme.TEXT_GOLD);

        JPanel itemsBox = new JPanel();
        itemsBox.setLayout(new BoxLayout(itemsBox, BoxLayout.Y_AXIS));
        itemsBox.setOpaque(false);

        List<String> summaries = connectionPort.getDiscoveredConnectionSummaries("CASE_103");
        for (int i = 0; i < summaries.size(); i++) {
            Color bulletColor = (i == 1) ? Theme.TEAL_ACCENT : (i == 2 ? Theme.GOLD_PRIMARY : (i == 3 ? Theme.RED_CONFLICT : Theme.TEXT_PRIMARY));
            itemsBox.add(createConnectionRow(summaries.get(i), bulletColor));
            if (i < summaries.size() - 1) {
                itemsBox.add(Box.createRigidArea(new Dimension(0, 14)));
            }
        }

        CustomButton btnRun = new CustomButton("RUN ANALYSIS", CustomButton.ButtonStyle.GOLD);
        btnRun.setPreferredSize(new Dimension(280, 42));
        btnRun.addActionListener(e -> ScreenManager.getInstance().showScreen(ScreenManager.CASE_ANALYSIS));

        rightCard.add(titleLbl, BorderLayout.NORTH);
        rightCard.add(itemsBox, BorderLayout.CENTER);
        rightCard.add(btnRun, BorderLayout.SOUTH);

        mainSplit.add(rightCard, BorderLayout.EAST);
        add(mainSplit, BorderLayout.CENTER);
    }

    private JPanel createConnectionRow(String text, Color accentColor) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setOpaque(false);

        JLabel bullet = new JLabel("•");
        bullet.setFont(Theme.bold(16));
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
}
