package com.mysteryvault.ui.swing.screens;

import com.mysteryvault.ui.swing.components.*;
import com.mysteryvault.ui.swing.mock.MockProfileViewPort;
import com.mysteryvault.ui.swing.navigation.RefreshableScreen;
import com.mysteryvault.ui.swing.navigation.ScreenManager;
import com.mysteryvault.ui.swing.ports.ProfileViewPort;
import com.mysteryvault.ui.swing.theme.AssetManager;
import com.mysteryvault.ui.swing.theme.Theme;
import com.mysteryvault.ui.swing.viewmodel.ProfileViewModel;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

/**
 * Screen 2: Detective Profile Screen.
 * Implements RefreshableScreen to automatically reflect persisted progression immediately.
 */
public class DetectiveProfilePanel extends JPanel implements RefreshableScreen {

    private final ProfileViewPort profilePort;

    private JLabel lblRank;
    private JLabel lblXp;
    private JPanel xpBarContainer;
    private JLabel valSolved;
    private JLabel valAccuracy;
    private JLabel valAttempted;
    private JLabel valBestScore;

    public DetectiveProfilePanel(ProfileViewPort profilePort) {
        this.profilePort = profilePort != null ? profilePort : new MockProfileViewPort();
        setOpaque(true);
        setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());

        initUI();
    }

    public DetectiveProfilePanel() {
        this(new MockProfileViewPort());
    }

    @Override
    public void refreshScreen(String caseId) {
        ProfileViewModel profile = profilePort.getPlayerProfile();
        if (profile != null) {
            if (lblRank != null) lblRank.setText("Rank: " + profile.getRank());
            if (lblXp != null) lblXp.setText(profile.getXp() + " XP");

            if (xpBarContainer != null) {
                xpBarContainer.removeAll();
                int progress = Math.min(100, (profile.getXp() * 100) / 1000);
                StatBar bar = new StatBar(progress, Theme.GOLD_PRIMARY);
                bar.setPreferredSize(new Dimension(270, 8));
                xpBarContainer.add(bar, BorderLayout.CENTER);
                xpBarContainer.revalidate();
                xpBarContainer.repaint();
            }

            if (valSolved != null) valSolved.setText(profile.getCasesSolved() + "/" + profile.getTotalCases());
            if (valAccuracy != null) valAccuracy.setText(profile.getAccuracyDisplay());
            if (valAttempted != null) valAttempted.setText(String.valueOf(profile.getCasesAttempted()));
            if (valBestScore != null) valBestScore.setText(String.valueOf(profile.getBestScore()));
        }
    }

    private void initUI() {
        ScreenHeader header = new ScreenHeader("2. DETECTIVE PROFILE");
        add(header, BorderLayout.NORTH);

        ProfileViewModel profile = profilePort.getPlayerProfile();

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        GlassPanel profileCard = new GlassPanel();
        profileCard.setPreferredSize(new Dimension(540, 520));
        profileCard.setLayout(new BoxLayout(profileCard, BoxLayout.Y_AXIS));
        profileCard.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));

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

        JLabel lblTitle = new JLabel("DETECTIVE PROFILE");
        lblTitle.setFont(Theme.titleFont(22));
        lblTitle.setForeground(Theme.TEXT_PRIMARY);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblRank = new JLabel("Rank: " + profile.getRank());
        lblRank.setFont(Theme.bold(14));
        lblRank.setForeground(Theme.TEXT_GOLD);
        lblRank.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel xpPanel = new JPanel(new BorderLayout(8, 0));
        xpPanel.setOpaque(false);
        xpPanel.setMaximumSize(new Dimension(340, 24));
        xpPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblXp = new JLabel(profile.getXp() + " XP");
        lblXp.setFont(Theme.bold(12));
        lblXp.setForeground(Theme.TEXT_GOLD);

        xpBarContainer = new JPanel(new BorderLayout());
        xpBarContainer.setOpaque(false);
        int initialProgress = Math.min(100, (profile.getXp() * 100) / 1000);
        StatBar xpBar = new StatBar(initialProgress, Theme.GOLD_PRIMARY);
        xpBar.setPreferredSize(new Dimension(270, 8));
        xpBarContainer.add(xpBar, BorderLayout.CENTER);

        xpPanel.add(xpBarContainer, BorderLayout.CENTER);
        xpPanel.add(lblXp, BorderLayout.EAST);

        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 12, 0));
        statsGrid.setOpaque(false);
        statsGrid.setMaximumSize(new Dimension(480, 80));
        statsGrid.setAlignmentX(Component.CENTER_ALIGNMENT);

        valSolved = new JLabel(profile.getCasesSolved() + "/" + profile.getTotalCases(), SwingConstants.CENTER);
        valAccuracy = new JLabel(profile.getAccuracyDisplay(), SwingConstants.CENTER);
        valAttempted = new JLabel(String.valueOf(profile.getCasesAttempted()), SwingConstants.CENTER);
        valBestScore = new JLabel(String.valueOf(profile.getBestScore()), SwingConstants.CENTER);

        statsGrid.add(createStatCell("Cases Solved", valSolved, Theme.TEXT_PRIMARY));
        statsGrid.add(createStatCell("Accuracy", valAccuracy, Theme.TEAL_ACCENT));
        statsGrid.add(createStatCell("Cases Attempted", valAttempted, Theme.TEXT_PRIMARY));
        statsGrid.add(createStatCell("Best Score", valBestScore, Theme.TEXT_GOLD));

        CustomButton btnContinue = new CustomButton("CONTINUE", CustomButton.ButtonStyle.GOLD);
        btnContinue.setPreferredSize(new Dimension(240, 42));
        btnContinue.setMaximumSize(new Dimension(240, 42));
        btnContinue.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnContinue.addActionListener(e -> ScreenManager.getInstance().showScreen(ScreenManager.CASE_SELECTION));

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

    private JPanel createStatCell(String title, JLabel valLbl, Color valColor) {
        GlassPanel cell = new GlassPanel(new BorderLayout(0, 4));
        cell.setBackgroundColor(Theme.CARD_INNER);
        cell.setBorderColor(Theme.BORDER_SUBTLE);
        cell.setCornerRadius(8);
        cell.setBorder(BorderFactory.createEmptyBorder(10, 6, 10, 6));

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
