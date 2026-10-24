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
import java.util.ArrayList;
import java.util.List;

public class InvestigationHubPanel extends JPanel implements com.mysteryvault.ui.swing.navigation.RefreshableScreen {

    private final com.mysteryvault.ui.swing.ports.CaseViewPort casePort;
    private JLabel titleLbl;
    private JLabel subLbl;

    public InvestigationHubPanel() {
        this(null);
    }

    public InvestigationHubPanel(com.mysteryvault.ui.swing.ports.CaseViewPort casePort) {
        this.casePort = casePort;
        setOpaque(true);
        setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());

        initUI();
    }

    @Override
    public void refreshScreen(String caseId) {
        if (casePort != null) {
            com.mysteryvault.ui.swing.viewmodel.CaseCardViewModel active = casePort.getActiveCase();
            if (active != null) {
                if (titleLbl != null) titleLbl.setText(active.getTitle().toUpperCase());
                if (subLbl != null) subLbl.setText("Active Case: " + active.getId() + " • Status: " + active.getStatusBadge() + " • " + active.getDuration());
            }
        }
    }

    private final java.util.List<HubTile> hubTiles = new ArrayList<>();

    public java.util.List<HubTile> getHubTiles() {
        return java.util.Collections.unmodifiableList(hubTiles);
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

        titleLbl = new JLabel("CASE #101 – THE MISSING DIAMOND");
        titleLbl.setFont(Theme.titleFont(18));
        titleLbl.setForeground(Theme.TEXT_PRIMARY);

        subLbl = new JLabel("Active Mystery Investigation • Uncover the truth.");
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

        hubTiles.clear();
        addTile(actionGrid, new HubTile("SUSPECTS", "suspects", false, () ->
                ScreenManager.getInstance().showScreen(ScreenManager.SUSPECT_SCREEN)));

        addTile(actionGrid, new HubTile("EVIDENCE", "evidence", false, () ->
                ScreenManager.getInstance().showScreen(ScreenManager.EVIDENCE_INVESTIGATION)));

        addTile(actionGrid, new HubTile("TIMELINE", "timeline", false, () ->
                ScreenManager.getInstance().showScreen(ScreenManager.INVESTIGATION_TIMELINE)));

        addTile(actionGrid, new HubTile("INTERROGATE", "interrogate", false, () ->
                ScreenManager.getInstance().showScreen(ScreenManager.INTERROGATION)));

        addTile(actionGrid, new HubTile("CONNECTIONS", "connections", false, () ->
                ScreenManager.getInstance().showScreen(ScreenManager.CASE_CONNECTION_ENGINE)));

        addTile(actionGrid, new HubTile("ANALYZE CASE", "analyze", false, () ->
                ScreenManager.getInstance().showScreen(ScreenManager.CASE_ANALYSIS)));

        addTile(actionGrid, new HubTile("FINAL DEDUCTION", "clues", false, () ->
                ScreenManager.getInstance().showScreen(ScreenManager.FINAL_DEDUCTION)));

        addTile(actionGrid, new HubTile("PROFILE", "search", false, () ->
                ScreenManager.getInstance().showScreen(ScreenManager.DETECTIVE_PROFILE)));

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

    private void addTile(JPanel grid, HubTile tile) {
        hubTiles.add(tile);
        grid.add(tile);
    }

    /**
     * Reusable HubTile component implementing Change 2:
     * Every tile has a visible gold border in its normal state,
     * brighter gold border and subtle highlight on hover,
     * stronger gold border and subtle gold tint when selected.
     */
    public static class HubTile extends JPanel {
        private final String title;
        private final String iconType;
        private final Runnable onClick;
        private boolean isHovered = false;
        private boolean isSelected = false;

        public static final Color GOLD_BORDER_DEFAULT = Theme.BORDER_GOLD;
        public static final Color GOLD_BORDER_HOVER = new Color(255, 218, 125);
        public static final Color GOLD_BORDER_SELECTED = new Color(255, 230, 150);

        private static final Color BG_DEFAULT = Theme.CARD_BG;
        private static final Color BG_HOVER = Theme.CARD_BG_HOVER;
        private static final Color BG_SELECTED = new Color(30, 42, 64, 250);
        private static final Color TINT_GOLD_HOVER = new Color(212, 163, 89, 25);
        private static final Color TINT_GOLD_SELECTED = new Color(212, 163, 89, 45);

        public HubTile(String title, String iconType, boolean isSelected, Runnable onClick) {
            this.title = title;
            this.iconType = iconType;
            this.isSelected = isSelected;
            this.onClick = onClick;

            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(16, 12, 16, 12));

            JPanel iconCanvas = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    Theme.applyQualityHints(g2);
                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2;
                    drawHubTileIcon(g2, iconType, cx, cy, isHovered || isSelected);
                    g2.dispose();
                }
            };
            iconCanvas.setOpaque(false);

            JLabel label = new JLabel(title, SwingConstants.CENTER);
            label.setFont(Theme.bold(13));
            label.setForeground(isSelected ? Theme.TEXT_GOLD : Theme.TEXT_PRIMARY);

            add(iconCanvas, BorderLayout.CENTER);
            add(label, BorderLayout.SOUTH);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    label.setForeground(Theme.TEXT_GOLD);
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    label.setForeground(isSelected ? Theme.TEXT_GOLD : Theme.TEXT_PRIMARY);
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (onClick != null) onClick.run();
                }
            });
        }

        public String getTitle() {
            return title;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            this.isSelected = selected;
            repaint();
        }

        public Color getCurrentBorderColor() {
            if (isSelected) return GOLD_BORDER_SELECTED;
            if (isHovered) return GOLD_BORDER_HOVER;
            return GOLD_BORDER_DEFAULT;
        }

        public boolean hasGoldBorder() {
            return true;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.applyQualityHints(g2);

            int w = getWidth();
            int h = getHeight();
            RoundRectangle2D shape = new RoundRectangle2D.Float(1.5f, 1.5f, w - 3f, h - 3f, 14, 14);

            // 1. Dark navy/charcoal background
            Color bg = isSelected ? BG_SELECTED : (isHovered ? BG_HOVER : BG_DEFAULT);
            g2.setColor(bg);
            g2.fill(shape);

            // Subtle gold highlight on hover / selection
            if (isSelected) {
                g2.setColor(TINT_GOLD_SELECTED);
                g2.fill(shape);
            } else if (isHovered) {
                g2.setColor(TINT_GOLD_HOVER);
                g2.fill(shape);
            }

            // 2. Visible gold border in ALL states
            if (isSelected) {
                g2.setColor(GOLD_BORDER_SELECTED);
                g2.setStroke(new BasicStroke(2.5f));
            } else if (isHovered) {
                g2.setColor(GOLD_BORDER_HOVER);
                g2.setStroke(new BasicStroke(2.2f));
            } else {
                g2.setColor(GOLD_BORDER_DEFAULT);
                g2.setStroke(new BasicStroke(1.6f));
            }
            g2.draw(shape);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static void drawHubTileIcon(Graphics2D g2, String type, int cx, int cy, boolean isGold) {
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
