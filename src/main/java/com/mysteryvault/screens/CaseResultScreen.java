package com.mysteryvault.screens;

import com.mysteryvault.components.*;
import com.mysteryvault.navigation.ScreenManager;
import com.mysteryvault.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class CaseResultScreen extends JPanel {

    public CaseResultScreen() {
        setOpaque(true);
        setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());

        initUI();
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

        // Trophy & Header
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

                // Gold trophy drawing
                g2.setColor(Theme.GOLD_PRIMARY);
                // Cup
                g2.fillArc(cx - 16, cy - 18, 32, 26, 0, -180);
                g2.fillRect(cx - 16, cy - 18, 32, 10);
                // Handles
                g2.setStroke(new BasicStroke(2.2f));
                g2.drawArc(cx - 24, cy - 16, 12, 16, 90, 180);
                g2.drawArc(cx + 12, cy - 16, 12, 16, 270, 180);
                // Stem and Base
                g2.fillRect(cx - 3, cy + 2, 6, 8);
                g2.fillRoundRect(cx - 14, cy + 10, 28, 5, 2, 2);

                g2.dispose();
            }
        };
        trophyPanel.setOpaque(false);
        trophyPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel solvedLbl = new JLabel("CASE SOLVED");
        solvedLbl.setFont(Theme.titleFont(24));
        solvedLbl.setForeground(Theme.TEAL_ACCENT);
        solvedLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel caseNameLbl = new JLabel("THE POISONED DRINK");
        caseNameLbl.setFont(Theme.bold(14));
        caseNameLbl.setForeground(Theme.TEXT_PRIMARY);
        caseNameLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Investigation Score Pill
        JPanel scorePill = new JPanel() {
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
                String text = "Investigation Score: 92 / 100";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text, (w - fm.stringWidth(text)) / 2, (h - fm.getHeight()) / 2 + fm.getAscent());

                g2.dispose();
            }
        };
        scorePill.setOpaque(false);
        scorePill.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 2x2 Stats Grid
        JPanel stats2x2 = new JPanel(new GridLayout(2, 2, 24, 12));
        stats2x2.setOpaque(false);
        stats2x2.setMaximumSize(new Dimension(540, 70));
        stats2x2.setAlignmentX(Component.CENTER_ALIGNMENT);

        stats2x2.add(createStatLine("Evidence Found:", "8 / 8", Theme.TEAL_ACCENT));
        stats2x2.add(createStatLine("Contradictions Found:", "3 / 3", Theme.TEAL_ACCENT));
        stats2x2.add(createStatLine("Important Clues:", "6 / 6", Theme.TEAL_ACCENT));
        stats2x2.add(createStatLine("Wrong Accusations:", "0", Theme.GOLD_PRIMARY));

        // Master Detective Banner
        JLabel rankBanner = new JLabel("✦ MASTER DETECTIVE ✦");
        rankBanner.setFont(Theme.titleFont(16));
        rankBanner.setForeground(Theme.TEXT_GOLD);
        rankBanner.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Bottom Action Buttons: NEXT CASE, CASE REPORT, RETURN TO MENU
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        buttonRow.setOpaque(false);
        buttonRow.setMaximumSize(new Dimension(560, 44));
        buttonRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        CustomButton btnNext = new CustomButton("NEXT CASE", CustomButton.ButtonStyle.DARK);
        btnNext.addActionListener(e -> ScreenManager.getInstance().showScreen(ScreenManager.CASE_SELECTION));

        CustomButton btnReport = new CustomButton("CASE REPORT", CustomButton.ButtonStyle.DARK);
        btnReport.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Official Case Dossier:\n- Culprit: Meera (Accountant)\n- Motive: Financial embezzlement covering\n- Status: Confessed and remanded to custody\n- Commendation: Awarded Gold Badge of Service",
                "Case Dossier Report", JOptionPane.INFORMATION_MESSAGE));

        CustomButton btnReturn = new CustomButton("RETURN TO MENU", CustomButton.ButtonStyle.GOLD);
        btnReturn.addActionListener(e -> ScreenManager.getInstance().showScreen(ScreenManager.MAIN_MENU));

        buttonRow.add(btnNext);
        buttonRow.add(btnReport);
        buttonRow.add(btnReturn);

        // Assemble victory card
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

    private JPanel createStatLine(String label, String value, Color valColor) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.regular(12));
        lbl.setForeground(Theme.TEXT_SECONDARY);

        JLabel val = new JLabel(value);
        val.setFont(Theme.bold(13));
        val.setForeground(valColor);

        p.add(lbl, BorderLayout.WEST);
        p.add(val, BorderLayout.EAST);
        return p;
    }
}
