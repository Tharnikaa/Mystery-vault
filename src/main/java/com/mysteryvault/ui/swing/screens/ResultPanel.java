package com.mysteryvault.ui.swing.screens;

import com.mysteryvault.service.dto.DeductionResult;
import com.mysteryvault.ui.swing.components.*;
import com.mysteryvault.ui.swing.mock.MockDeductionViewPort;
import com.mysteryvault.ui.swing.mock.MockReportViewPort;
import com.mysteryvault.ui.swing.navigation.ScreenManager;
import com.mysteryvault.ui.swing.ports.DeductionViewPort;
import com.mysteryvault.ui.swing.ports.ReportViewPort;
import com.mysteryvault.ui.swing.theme.Theme;
import com.mysteryvault.ui.swing.viewmodel.ReportViewModel;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class ResultPanel extends JPanel {

    private final DeductionViewPort deductionPort;
    private final ReportViewPort reportPort;

    private JLabel solvedLbl;
    private JLabel caseNameLbl;
    private JLabel rankBanner;
    private JPanel scorePill;
    private JLabel lblEvidence;
    private JLabel lblContradictions;
    private JLabel lblClues;
    private JLabel lblWrong;

    public ResultPanel(DeductionViewPort deductionPort, ReportViewPort reportPort) {
        this.deductionPort = deductionPort != null ? deductionPort : new MockDeductionViewPort();
        this.reportPort = reportPort != null ? reportPort : new MockReportViewPort();

        setOpaque(true);
        setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());

        initUI();
    }

    public ResultPanel() {
        this(new MockDeductionViewPort(), new MockReportViewPort());
    }

    private void initUI() {
        ScreenHeader header = new ScreenHeader("12. CASE RESULT");
        add(header, BorderLayout.NORTH);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        GlassPanel victoryCard = new GlassPanel();
        victoryCard.setLayout(new BoxLayout(victoryCard, BoxLayout.Y_AXIS));
        victoryCard.setPreferredSize(new Dimension(680, 520));
        victoryCard.setBorder(BorderFactory.createEmptyBorder(24, 36, 24, 36));

        JPanel trophyPanel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(60, 48);
            }

            @Override
            public Dimension getMaximumSize() {
                return new Dimension(60, 48);
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.applyQualityHints(g2);

                int cx = getWidth() / 2;
                int cy = getHeight() / 2;

                g2.setColor(Theme.GOLD_PRIMARY);
                g2.fillArc(cx - 16, cy - 18, 32, 26, 0, -180);
                g2.fillRect(cx - 16, cy - 18, 32, 10);
                g2.setStroke(new BasicStroke(2.2f));
                g2.drawArc(cx - 24, cy - 16, 12, 16, 90, 180);
                g2.drawArc(cx + 12, cy - 16, 12, 16, 270, 180);
                g2.fillRect(cx - 3, cy + 2, 6, 8);
                g2.fillRoundRect(cx - 14, cy + 10, 28, 5, 2, 2);

                g2.dispose();
            }
        };
        trophyPanel.setOpaque(false);
        trophyPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        DeductionResult res = deductionPort.getLastResult();

        solvedLbl = new JLabel(res != null ? res.getVerdictTitle() : "CASE SOLVED");
        solvedLbl.setFont(Theme.titleFont(24));
        solvedLbl.setForeground(res != null && res.isSolved() ? Theme.TEAL_ACCENT : Theme.RED_CONFLICT);
        solvedLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        caseNameLbl = new JLabel(res != null ? res.getCaseTitle() : "THE POISONED DRINK");
        caseNameLbl.setFont(Theme.bold(14));
        caseNameLbl.setForeground(Theme.TEXT_PRIMARY);
        caseNameLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        scorePill = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(280, 34);
            }

            @Override
            public Dimension getMaximumSize() {
                return new Dimension(280, 34);
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.applyQualityHints(g2);

                int w = getWidth();
                int h = getHeight();
                RoundRectangle2D round = new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 8, 8);
                g2.setColor(new Color(212, 163, 89, 30));
                g2.fill(round);
                g2.setColor(Theme.BORDER_GOLD);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(round);

                g2.setFont(Theme.bold(13));
                g2.setColor(Theme.TEXT_GOLD);
                DeductionResult r = deductionPort.getLastResult();
                String text = "Investigation Score: " + (r != null ? r.getScore() : 92) + " / 100";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text, (w - fm.stringWidth(text)) / 2, (h - fm.getHeight()) / 2 + fm.getAscent());

                g2.dispose();
            }
        };
        scorePill.setOpaque(false);
        scorePill.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel stats2x2 = new JPanel(new GridLayout(2, 2, 24, 12));
        stats2x2.setOpaque(false);
        stats2x2.setMaximumSize(new Dimension(540, 70));
        stats2x2.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblEvidence = new JLabel(res != null ? (res.getEvidenceFoundCount() + " / " + res.getTotalEvidenceCount()) : "8 / 8");
        lblEvidence.setFont(Theme.bold(13));
        lblEvidence.setForeground(Theme.TEAL_ACCENT);

        lblContradictions = new JLabel(res != null ? (res.getContradictionsFound() + " / " + res.getTotalContradictions()) : "3 / 3");
        lblContradictions.setFont(Theme.bold(13));
        lblContradictions.setForeground(Theme.TEAL_ACCENT);

        lblClues = new JLabel(res != null ? (res.getImportantCluesCount() + " / " + res.getTotalCluesCount()) : "6 / 6");
        lblClues.setFont(Theme.bold(13));
        lblClues.setForeground(Theme.TEAL_ACCENT);

        lblWrong = new JLabel(res != null ? String.valueOf(res.getWrongAccusations()) : "0");
        lblWrong.setFont(Theme.bold(13));
        lblWrong.setForeground(Theme.GOLD_PRIMARY);

        stats2x2.add(createStatLine("Evidence Found:", lblEvidence));
        stats2x2.add(createStatLine("Contradictions Found:", lblContradictions));
        stats2x2.add(createStatLine("Important Clues:", lblClues));
        stats2x2.add(createStatLine("Wrong Accusations:", lblWrong));

        rankBanner = new JLabel(res != null ? ("✦ " + res.getRankTitle() + " ✦") : "✦ MASTER DETECTIVE ✦");
        rankBanner.setFont(Theme.titleFont(16));
        rankBanner.setForeground(Theme.TEXT_GOLD);
        rankBanner.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        buttonRow.setOpaque(false);
        buttonRow.setMaximumSize(new Dimension(560, 44));
        buttonRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        CustomButton btnNext = new CustomButton("NEXT CASE", CustomButton.ButtonStyle.DARK);
        btnNext.addActionListener(e -> ScreenManager.getInstance().showScreen(ScreenManager.CASE_SELECTION));

        CustomButton btnReport = new CustomButton("CASE REPORT", CustomButton.ButtonStyle.DARK);
        btnReport.addActionListener(e -> {
            ReportViewModel report = reportPort.generateReport("CASE_103");
            JTextArea area = new JTextArea(report.getOfficialSummary());
            area.setFont(new Font("Consolas", Font.PLAIN, 12));
            area.setEditable(false);
            area.setBackground(new Color(20, 26, 38));
            area.setForeground(Theme.TEXT_PRIMARY);
            area.setCaretColor(Theme.TEXT_GOLD);
            area.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

            JScrollPane sp = new JScrollPane(area);
            sp.setPreferredSize(new Dimension(520, 260));
            sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER_GOLD, 1));

            JOptionPane.showMessageDialog(this, sp, "Case Dossier Report", JOptionPane.INFORMATION_MESSAGE);
        });

        CustomButton btnReturn = new CustomButton("RETURN TO MENU", CustomButton.ButtonStyle.GOLD);
        btnReturn.addActionListener(e -> ScreenManager.getInstance().showScreen(ScreenManager.MAIN_MENU));

        buttonRow.add(btnNext);
        buttonRow.add(btnReport);
        buttonRow.add(btnReturn);

        victoryCard.add(trophyPanel);
        victoryCard.add(Box.createRigidArea(new Dimension(0, 4)));
        victoryCard.add(solvedLbl);
        victoryCard.add(Box.createRigidArea(new Dimension(0, 4)));
        victoryCard.add(caseNameLbl);
        victoryCard.add(Box.createRigidArea(new Dimension(0, 16)));
        victoryCard.add(scorePill);
        victoryCard.add(Box.createRigidArea(new Dimension(0, 20)));
        victoryCard.add(stats2x2);
        victoryCard.add(Box.createRigidArea(new Dimension(0, 20)));
        victoryCard.add(rankBanner);
        victoryCard.add(Box.createRigidArea(new Dimension(0, 24)));
        victoryCard.add(buttonRow);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        centerWrapper.add(victoryCard, gbc);

        add(centerWrapper, BorderLayout.CENTER);
    }

    private JPanel createStatLine(String label, JLabel valLbl) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.regular(12));
        lbl.setForeground(Theme.TEXT_SECONDARY);

        p.add(lbl, BorderLayout.WEST);
        p.add(valLbl, BorderLayout.EAST);
        return p;
    }
}
