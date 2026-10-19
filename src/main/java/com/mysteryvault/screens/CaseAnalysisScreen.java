package com.mysteryvault.screens;

import com.mysteryvault.components.*;
import com.mysteryvault.navigation.ScreenManager;
import com.mysteryvault.theme.Theme;

import javax.swing.*;
import java.awt.*;

public class CaseAnalysisScreen extends JPanel {

    public CaseAnalysisScreen() {
        setOpaque(true);
        setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());

        initUI();
    }

    private void initUI() {
        ScreenHeader header = new ScreenHeader("10. CASE ANALYSIS");
        add(header, BorderLayout.NORTH);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        GlassPanel mainCard = new GlassPanel(new BorderLayout(0, 24));
        mainCard.setPreferredSize(new Dimension(840, 500));
        mainCard.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));

        // Two Column Body: Left Progress Bars, Right Results Stats
        JPanel columnsPanel = new JPanel(new GridLayout(1, 2, 36, 0));
        columnsPanel.setOpaque(false);

        // Left Column: Analysis Bars
        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setOpaque(false);

        leftCol.add(createAnalysisRow("Timeline Analysis", 100, "COMPLETE", Theme.TEAL_ACCENT));
        leftCol.add(Box.createRigidArea(new Dimension(0, 22)));
        leftCol.add(createAnalysisRow("Evidence Analysis", 100, "COMPLETE", Theme.TEAL_ACCENT));
        leftCol.add(Box.createRigidArea(new Dimension(0, 22)));
        leftCol.add(createAnalysisRow("Statement Analysis", 100, "COMPLETE", Theme.TEAL_ACCENT));
        leftCol.add(Box.createRigidArea(new Dimension(0, 22)));
        leftCol.add(createAnalysisRow("Connection Analysis", 90, "90%", Theme.GOLD_PRIMARY));

        columnsPanel.add(leftCol);

        // Right Column: Results Stats Card
        GlassPanel rightCol = new GlassPanel(new BorderLayout(0, 16));
        rightCol.setBackgroundColor(Theme.CARD_INNER);
        rightCol.setBorderColor(Theme.BORDER_SUBTLE);
        rightCol.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel resTitle = new JLabel("RESULTS", SwingConstants.CENTER);
        resTitle.setFont(Theme.titleFont(16));
        resTitle.setForeground(Theme.TEXT_GOLD);
        rightCol.add(resTitle, BorderLayout.NORTH);

        JPanel statList = new JPanel(new GridLayout(4, 1, 0, 12));
        statList.setOpaque(false);

        statList.add(createResultStat("Connections Found:", "8", Theme.TEAL_ACCENT));
        statList.add(createResultStat("Contradictions:", "3", Theme.RED_CONFLICT));
        statList.add(createResultStat("Important Evidence:", "5", Theme.TEAL_ACCENT));
        statList.add(createResultStat("Suspicious Characters:", "2", Theme.GOLD_PRIMARY));

        rightCol.add(statList, BorderLayout.CENTER);
        columnsPanel.add(rightCol);

        mainCard.add(columnsPanel, BorderLayout.CENTER);

        // Bottom Button: VIEW RESULTS -> leads to Final Deduction
        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        bottomRow.setOpaque(false);

        CustomButton btnView = new CustomButton("VIEW RESULTS", CustomButton.ButtonStyle.GOLD);
        btnView.setPreferredSize(new Dimension(240, 42));
        btnView.addActionListener(e -> ScreenManager.getInstance().showScreen(ScreenManager.FINAL_DEDUCTION));
        bottomRow.add(btnView);

        mainCard.add(bottomRow, BorderLayout.SOUTH);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        centerWrapper.add(mainCard, gbc);

        add(centerWrapper, BorderLayout.CENTER);
    }

    private JPanel createAnalysisRow(String title, int progress, String statusBadge, Color barColor) {
        JPanel row = new JPanel(new BorderLayout(10, 6));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(360, 48));

        // Top labels
        JPanel headerLine = new JPanel(new BorderLayout());
        headerLine.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(Theme.bold(13));
        titleLbl.setForeground(Theme.TEXT_PRIMARY);

        JLabel statusLbl = new JLabel(statusBadge);
        statusLbl.setFont(Theme.bold(12));
        statusLbl.setForeground(barColor);

        headerLine.add(titleLbl, BorderLayout.WEST);
        headerLine.add(statusLbl, BorderLayout.EAST);

        // Bar
        StatBar bar = new StatBar(progress, barColor);
        bar.setPreferredSize(new Dimension(340, 8));

        row.add(headerLine, BorderLayout.NORTH);
        row.add(bar, BorderLayout.SOUTH);
        return row;
    }

    private JPanel createResultStat(String label, String value, Color valColor) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.regular(13));
        lbl.setForeground(Theme.TEXT_SECONDARY);

        JLabel val = new JLabel(value);
        val.setFont(Theme.bold(15));
        val.setForeground(valColor);

        p.add(lbl, BorderLayout.WEST);
        p.add(val, BorderLayout.EAST);
        return p;
    }
}
