package com.mysteryvault.screens;

import com.mysteryvault.components.*;
import com.mysteryvault.navigation.ScreenManager;
import com.mysteryvault.theme.AssetManager;
import com.mysteryvault.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

public class DetectiveProfileScreen extends JPanel {

    public DetectiveProfileScreen() {
        setOpaque(true);
        setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());

        initUI();
    }

    private void initUI() {
        // Screen Header
        ScreenHeader header = new ScreenHeader("2. DETECTIVE PROFILE");
        add(header, BorderLayout.NORTH);

        // Center card container
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        GlassPanel profileCard = new GlassPanel();
        profileCard.setPreferredSize(new Dimension(540, 520));
        profileCard.setLayout(new BoxLayout(profileCard, BoxLayout.Y_AXIS));
        profileCard.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));

        // Circular Detective Avatar
        JPanel avatarPanel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(130, 130);
            }

            @Override
            public Dimension getMaximumSize() {
                return new Dimension(130, 130);
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.applyQualityHints(g2);

                int size = 120;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;

                // Subtle glow
                g2.setColor(new Color(212, 163, 89, 60));
                g2.fill(new Ellipse2D.Float(x - 4, y - 4, size + 8, size + 8));

                BufferedImage portrait = AssetManager.getImage("detective_portrait.jpg");
                BufferedImage circular = AssetManager.getCircularImage(portrait, size, Theme.BORDER_GOLD, 3);
                g2.drawImage(circular, x, y, null);

                g2.dispose();
            }
        };
        avatarPanel.setOpaque(false);
        avatarPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Title: DETECTIVE PROFILE
        JLabel lblTitle = new JLabel("DETECTIVE PROFILE");
        lblTitle.setFont(Theme.titleFont(22));
        lblTitle.setForeground(Theme.TEXT_PRIMARY);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Rank: Detective
        JLabel lblRank = new JLabel("Rank: Detective");
        lblRank.setFont(Theme.bold(14));
        lblRank.setForeground(Theme.TEXT_GOLD);
        lblRank.setAlignmentX(Component.CENTER_ALIGNMENT);

        // XP bar container
        JPanel xpPanel = new JPanel(new BorderLayout(8, 0));
        xpPanel.setOpaque(false);
        xpPanel.setMaximumSize(new Dimension(340, 24));
        xpPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblXp = new JLabel("37 XP");
        lblXp.setFont(Theme.bold(12));
        lblXp.setForeground(Theme.TEXT_GOLD);

        StatBar xpBar = new StatBar(37, Theme.GOLD_PRIMARY);
        xpBar.setPreferredSize(new Dimension(270, 8));

        xpPanel.add(xpBar, BorderLayout.CENTER);
        xpPanel.add(lblXp, BorderLayout.EAST);

        // Stats 4-column card grid
        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 12, 0));
        statsGrid.setOpaque(false);
        statsGrid.setMaximumSize(new Dimension(480, 80));
        statsGrid.setAlignmentX(Component.CENTER_ALIGNMENT);

        statsGrid.add(createStatCell("Cases Solved", "4/20", Theme.TEXT_PRIMARY));
        statsGrid.add(createStatCell("Accuracy", "70%", Theme.TEAL_ACCENT));
        statsGrid.add(createStatCell("Cases Attempted", "6", Theme.TEXT_PRIMARY));
        statsGrid.add(createStatCell("Best Score", "72", Theme.TEXT_GOLD));

        // Action Button: CONTINUE
        CustomButton btnContinue = new CustomButton("CONTINUE", CustomButton.ButtonStyle.GOLD);
        btnContinue.setPreferredSize(new Dimension(240, 42));
        btnContinue.setMaximumSize(new Dimension(240, 42));
        btnContinue.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnContinue.addActionListener(e -> ScreenManager.getInstance().showScreen(ScreenManager.CASE_SELECTION));

        // Assemble profile card
        profileCard.add(avatarPanel);
        profileCard.add(Box.createRigidArea(new Dimension(0, 14)));
        profileCard.add(lblTitle);
        profileCard.add(Box.createRigidArea(new Dimension(0, 6)));
        profileCard.add(lblRank);
        profileCard.add(Box.createRigidArea(new Dimension(0, 16)));
        profileCard.add(xpPanel);
        profileCard.add(Box.createRigidArea(new Dimension(0, 24)));
        profileCard.add(statsGrid);
        profileCard.add(Box.createRigidArea(new Dimension(0, 32)));
        profileCard.add(btnContinue);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        centerWrapper.add(profileCard, gbc);

        add(centerWrapper, BorderLayout.CENTER);
    }

    private JPanel createStatCell(String title, String value, Color valColor) {
        GlassPanel cell = new GlassPanel(new BorderLayout(0, 4));
        cell.setBackgroundColor(Theme.CARD_INNER);
        cell.setBorderColor(Theme.BORDER_SUBTLE);
        cell.setCornerRadius(8);
        cell.setBorder(BorderFactory.createEmptyBorder(10, 6, 10, 6));

        JLabel valLbl = new JLabel(value, SwingConstants.CENTER);
        valLbl.setFont(Theme.bold(17));
        valLbl.setForeground(valColor);

        JLabel titleLbl = new JLabel("<html><center>" + title + "</center></html>", SwingConstants.CENTER);
        titleLbl.setFont(Theme.regular(10));
        titleLbl.setForeground(Theme.TEXT_MUTED);

        cell.add(valLbl, BorderLayout.CENTER);
        cell.add(titleLbl, BorderLayout.SOUTH);
        return cell;
    }
}
