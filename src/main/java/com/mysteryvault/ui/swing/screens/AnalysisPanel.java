package com.mysteryvault.ui.swing.screens;

import com.mysteryvault.ui.swing.components.*;
import com.mysteryvault.ui.swing.mock.MockAnalysisViewPort;
import com.mysteryvault.ui.swing.navigation.ScreenManager;
import com.mysteryvault.ui.swing.ports.AnalysisViewPort;
import com.mysteryvault.ui.swing.theme.Theme;
import com.mysteryvault.ui.swing.viewmodel.AnalysisViewModel;

import javax.swing.*;
import java.awt.*;

public class AnalysisPanel extends JPanel implements com.mysteryvault.ui.swing.navigation.RefreshableScreen {

    private final AnalysisViewPort analysisPort;
    private String currentCaseId = "CASE_101";

    public AnalysisPanel(AnalysisViewPort analysisPort) {
        this.analysisPort = analysisPort != null ? analysisPort : new MockAnalysisViewPort();
        setOpaque(true);
        setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());

        initUI(currentCaseId);
    }

    public AnalysisPanel() {
        this(new MockAnalysisViewPort());
    }

    @Override
    public void refreshScreen(String caseId) {
        this.currentCaseId = caseId != null ? caseId : "CASE_101";
        removeAll();
        initUI(this.currentCaseId);
        revalidate();
        repaint();
    }

    private void initUI(String caseId) {
        ScreenHeader header = new ScreenHeader("10. CASE ANALYSIS");
        add(header, BorderLayout.NORTH);

        AnalysisViewModel analysis = analysisPort.getAnalysisResults(caseId);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        GlassPanel mainCard = new GlassPanel(new BorderLayout(0, 24));
        mainCard.setPreferredSize(new Dimension(840, 500));
        mainCard.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));

        JPanel columnsPanel = new JPanel(new GridLayout(1, 2, 36, 0));
        columnsPanel.setOpaque(false);

        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setOpaque(false);

        leftCol.add(createAnalysisRow("Timeline Analysis", analysis.getTimelineProgress(), "COMPLETE", Theme.TEAL_ACCENT));
        leftCol.add(Box.createRigidArea(new Dimension(0, 22)));
        leftCol.add(createAnalysisRow("Evidence Analysis", analysis.getEvidenceProgress(), "COMPLETE", Theme.TEAL_ACCENT));
        leftCol.add(Box.createRigidArea(new Dimension(0, 22)));
        leftCol.add(createAnalysisRow("Statement Analysis", analysis.getStatementProgress(), "COMPLETE", Theme.TEAL_ACCENT));
        leftCol.add(Box.createRigidArea(new Dimension(0, 22)));
        leftCol.add(createAnalysisRow("Connection Analysis", analysis.getConnectionProgress(), analysis.getConnectionProgress() + "%", Theme.GOLD_PRIMARY));

        columnsPanel.add(leftCol);

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

        statList.add(createResultStat("Connections Found:", String.valueOf(analysis.getConnectionsFound()), Theme.TEAL_ACCENT));
        statList.add(createResultStat("Contradictions:", String.valueOf(analysis.getContradictions()), Theme.RED_CONFLICT));
        statList.add(createResultStat("Important Evidence:", String.valueOf(analysis.getImportantEvidence()), Theme.TEAL_ACCENT));
        statList.add(createResultStat("Suspicious Characters:", String.valueOf(analysis.getSuspiciousCharacters()), Theme.GOLD_PRIMARY));

        rightCol.add(statList, BorderLayout.CENTER);
        columnsPanel.add(rightCol);

        mainCard.add(columnsPanel, BorderLayout.CENTER);

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
