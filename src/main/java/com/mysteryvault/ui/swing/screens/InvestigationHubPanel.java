package com.mysteryvault.ui.swing.screens;

import com.mysteryvault.ui.swing.components.*;
import com.mysteryvault.ui.swing.navigation.ScreenManager;
import com.mysteryvault.ui.swing.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

public class InvestigationHubPanel extends JPanel {

    public InvestigationHubPanel() {
        setOpaque(true);
        setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());

        initUI();
    }

    private void initUI() {
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);
        topContainer.setBorder(BorderFactory.createEmptyBorder(12, 16, 0, 16));

        JPanel headerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        headerLeft.setOpaque(false);

        CustomButton backBtn = new CustomButton("<", CustomButton.ButtonStyle.ICON_BACK);
        backBtn.setPreferredSize(new Dimension(34, 34));
        backBtn.addActionListener(e -> ScreenManager.getInstance().showScreen(ScreenManager.CASE_SELECTION));
        headerLeft.add(backBtn);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 3));
        titleBox.setOpaque(false);

        JLabel titleLbl = new JLabel("CASE #103 – THE POISONED DRINK");
        titleLbl.setFont(Theme.titleFont(18));
        titleLbl.setForeground(Theme.TEXT_PRIMARY);

        JLabel subLbl = new JLabel("Find who poisoned the victim and determine the motive.");
        subLbl.setFont(Theme.regular(12));
        subLbl.setForeground(Theme.TEXT_MUTED);

        titleBox.add(titleLbl);
        titleBox.add(subLbl);
        headerLeft.add(titleBox);

        topContainer.add(headerLeft, BorderLayout.WEST);

        JPanel rightStatus = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.applyQualityHints(g2);
                int dotSize = 10;
                int dotX = getWidth() - 20;
                int dotY = (getHeight() - dotSize) / 2;
                g2.setColor(Theme.TEAL_GLOW);
                g2.fill(new Ellipse2D.Float(dotX - 3, dotY - 3, dotSize + 6, dotSize + 6));
                g2.setColor(Theme.TEAL_ACCENT);
                g2.fill(new Ellipse2D.Float(dotX, dotY, dotSize, dotSize));
                g2.dispose();
            }
        };
        rightStatus.setOpaque(false);
        rightStatus.setPreferredSize(new Dimension(50, 34));
        topContainer.add(rightStatus, BorderLayout.EAST);

        add(topContainer, BorderLayout.NORTH);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        JPanel actionGrid = new JPanel(new GridLayout(2, 4, 18, 18));
        actionGrid.setOpaque(false);
        actionGrid.setPreferredSize(new Dimension(860, 380));

        actionGrid.add(createHubTile("SUSPECTS", "suspects", false, () ->
                ScreenManager.getInstance().showScreen(ScreenManager.SUSPECT_SCREEN)));

        actionGrid.add(createHubTile("EVIDENCE", "evidence", true, () ->
                ScreenManager.getInstance().showScreen(ScreenManager.EVIDENCE_INVESTIGATION)));

        actionGrid.add(createHubTile("TIMELINE", "timeline", false, () ->
                ScreenManager.getInstance().showScreen(ScreenManager.INVESTIGATION_TIMELINE)));

        actionGrid.add(createHubTile("INTERROGATE", "interrogate", false, () ->
                ScreenManager.getInstance().showScreen(ScreenManager.INTERROGATION)));

        actionGrid.add(createHubTile("SEARCH", "search", false, () ->
                ScreenManager.getInstance().showScreen(ScreenManager.DETECTIVE_PROFILE)));

        actionGrid.add(createHubTile("CLUES", "clues", false, () ->
                ScreenManager.getInstance().showScreen(ScreenManager.EVIDENCE_INVESTIGATION)));

        actionGrid.add(createHubTile("CONNECTIONS", "connections", false, () ->
                ScreenManager.getInstance().showScreen(ScreenManager.CASE_CONNECTION_ENGINE)));

        actionGrid.add(createHubTile("ANALYZE CASE", "analyze", true, () ->
                ScreenManager.getInstance().showScreen(ScreenManager.CASE_ANALYSIS)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        centerWrapper.add(actionGrid, gbc);

        add(centerWrapper, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 48, 14));
        bottomBar.setOpaque(false);

        JLabel lblEvidence = new JLabel("Evidence: 8/12");
        lblEvidence.setFont(Theme.bold(13));
        lblEvidence.setForeground(Theme.TEXT_SECONDARY);

        JLabel lblClues = new JLabel("Clues: 8/10");
        lblClues.setFont(Theme.bold(13));
        lblClues.setForeground(Theme.TEXT_SECONDARY);

        JLabel lblScore = new JLabel("Score: 72");
        lblScore.setFont(Theme.bold(13));
        lblScore.setForeground(Theme.TEXT_GOLD);

        bottomBar.add(lblEvidence);
        bottomBar.add(lblClues);
        bottomBar.add(lblScore);

        add(bottomBar, BorderLayout.SOUTH);
    }

    private JPanel createHubTile(String title, String iconType, boolean isGoldHighlighted, Runnable onClick) {
        GlassPanel tile = new GlassPanel(new BorderLayout());
        tile.setCornerRadius(14);
        tile.setSelected(isGoldHighlighted);
        tile.setCursor(new Cursor(Cursor.HAND_CURSOR));
        tile.setBorder(BorderFactory.createEmptyBorder(16, 12, 16, 12));

        JPanel iconCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.applyQualityHints(g2);
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                drawIcon(g2, iconType, cx, cy, isGoldHighlighted);
                g2.dispose();
            }
        };
        iconCanvas.setOpaque(false);

        JLabel label = new JLabel(title, SwingConstants.CENTER);
        label.setFont(Theme.bold(13));
        label.setForeground(isGoldHighlighted ? Theme.TEXT_GOLD : Theme.TEXT_PRIMARY);

        tile.add(iconCanvas, BorderLayout.CENTER);
        tile.add(label, BorderLayout.SOUTH);

        tile.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (onClick != null) onClick.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                tile.setBackgroundColor(Theme.CARD_BG_HOVER);
                tile.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                tile.setBackgroundColor(Theme.CARD_BG);
                tile.repaint();
            }
        });

        return tile;
    }

    private void drawIcon(Graphics2D g2, String type, int cx, int cy, boolean isGold) {
        Color mainColor = isGold ? Theme.GOLD_PRIMARY : Theme.TEXT_SECONDARY;
        g2.setColor(mainColor);
        g2.setStroke(new BasicStroke(2.2f));

        switch (type) {
            case "suspects":
                g2.drawOval(cx - 10, cy - 16, 20, 20);
                g2.drawArc(cx - 18, cy + 6, 36, 18, 0, 180);
                g2.setColor(new Color(130, 145, 165));
                g2.drawOval(cx - 24, cy - 12, 14, 14);
                g2.drawOval(cx + 10, cy - 12, 14, 14);
                break;

            case "evidence":
                g2.drawRoundRect(cx - 15, cy - 18, 30, 36, 4, 4);
                g2.drawLine(cx - 8, cy - 8, cx + 8, cy - 8);
                g2.drawLine(cx - 8, cy - 2, cx + 8, cy - 2);
                g2.drawLine(cx - 8, cy + 4, cx + 4, cy + 4);
                g2.setColor(Theme.GOLD_PRIMARY);
                g2.drawOval(cx + 4, cy + 4, 10, 10);
                g2.drawLine(cx + 12, cy + 12, cx + 17, cy + 17);
                break;

            case "timeline":
                g2.drawRoundRect(cx - 16, cy - 16, 32, 32, 6, 6);
                g2.drawLine(cx - 16, cy - 6, cx + 16, cy - 6);
                g2.drawOval(cx - 1, cy + 2, 3, 3);
                g2.drawLine(cx - 10, cy - 20, cx - 10, cy - 14);
                g2.drawLine(cx + 10, cy - 20, cx + 10, cy - 14);
                break;

            case "interrogate":
                RoundRectangle2D bubble = new RoundRectangle2D.Float(cx - 16, cy - 16, 32, 26, 8, 8);
                g2.draw(bubble);
                g2.drawLine(cx - 8, cy - 8, cx + 8, cy - 8);
                g2.drawLine(cx - 8, cy - 2, cx + 2, cy - 2);
                break;

            case "search":
                g2.drawOval(cx - 12, cy - 12, 24, 24);
                g2.drawLine(cx + 7, cy + 7, cx + 17, cy + 17);
                break;

            case "clues":
                g2.drawOval(cx - 14, cy - 10, 16, 16);
                g2.drawLine(cx + 2, cy - 2, cx + 16, cy - 2);
                g2.drawLine(cx + 10, cy - 2, cx + 10, cy + 4);
                g2.drawLine(cx + 14, cy - 2, cx + 14, cy + 4);
                break;

            case "connections":
                g2.drawOval(cx - 16, cy - 12, 8, 8);
                g2.drawOval(cx + 8, cy - 12, 8, 8);
                g2.drawOval(cx - 4, cy + 8, 8, 8);
                g2.drawLine(cx - 8, cy - 8, cx, cy + 8);
                g2.drawLine(cx + 8, cy - 8, cx + 4, cy + 8);
                g2.drawLine(cx - 8, cy - 8, cx + 8, cy - 8);
                break;

            case "analyze":
            default:
                g2.drawOval(cx - 14, cy - 14, 26, 26);
                g2.drawLine(cx + 6, cy + 6, cx + 18, cy + 18);
                g2.setColor(Theme.GOLD_PRIMARY);
                g2.fillOval(cx - 4, cy - 4, 6, 6);
                break;
        }
    }
}
