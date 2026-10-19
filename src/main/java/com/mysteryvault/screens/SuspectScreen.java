package com.mysteryvault.screens;

import com.mysteryvault.components.*;
import com.mysteryvault.model.Suspect;
import com.mysteryvault.navigation.ScreenManager;
import com.mysteryvault.service.SuspectService;
import com.mysteryvault.theme.AssetManager;
import com.mysteryvault.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Screen 5: Suspect Screen (Module 2 UI integration).
 * Displays suspect and person profiles scoped strictly to the active selected case.
 */
public class SuspectScreen extends JPanel {

    private int selectedSuspectIndex = 0;
    private final List<GlassPanel> suspectCards = new ArrayList<>();
    private final List<Suspect> loadedSuspects = new ArrayList<>();
    private JPanel cardsContainer;

    public SuspectScreen() {
        setOpaque(true);
        setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());

        initUI();
    }

    private void initUI() {
        ScreenHeader header = new ScreenHeader("5. SUSPECT SCREEN");
        add(header, BorderLayout.NORTH);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        cardsContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 0));
        cardsContainer.setOpaque(false);

        loadSuspectsForActiveCase();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        centerWrapper.add(cardsContainer, gbc);

        add(centerWrapper, BorderLayout.CENTER);
    }

    public void reload() {
        cardsContainer.removeAll();
        suspectCards.clear();
        loadedSuspects.clear();
        loadSuspectsForActiveCase();
        cardsContainer.revalidate();
        cardsContainer.repaint();
    }

    private void loadSuspectsForActiveCase() {
        String caseId = ScreenManager.getInstance().getSelectedCaseId();
        SuspectService suspectService = ScreenManager.getInstance().getSuspectService();

        try {
            List<Suspect> suspects = suspectService.listSuspectsByCase(caseId);

            // If empty, also check fallback seed for Case #103
            if (suspects.isEmpty() && "CASE-103".equalsIgnoreCase(caseId)) {
                for (Suspect s : com.mysteryvault.service.SampleDataFactory.createSampleSuspects()) {
                    suspectService.createSuspect(s);
                    suspectService.linkSuspectToCase(caseId, s.getId());
                }
                suspects = suspectService.listSuspectsByCase(caseId);
            }

            if (suspects.isEmpty()) {
                JPanel emptyPanel = new JPanel();
                emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
                emptyPanel.setOpaque(false);

                JLabel lblEmpty = new JLabel("No suspects registered for Case: " + caseId);
                lblEmpty.setFont(Theme.bold(16));
                lblEmpty.setForeground(Theme.TEXT_MUTED);
                lblEmpty.setAlignmentX(Component.CENTER_ALIGNMENT);

                CustomButton btnRefresh = new CustomButton("RELOAD SUSPECTS", CustomButton.ButtonStyle.DARK);
                btnRefresh.setAlignmentX(Component.CENTER_ALIGNMENT);
                btnRefresh.addActionListener(e -> reload());

                emptyPanel.add(lblEmpty);
                emptyPanel.add(Box.createVerticalStrut(14));
                emptyPanel.add(btnRefresh);

                cardsContainer.add(emptyPanel);
                return;
            }

            loadedSuspects.addAll(suspects);

            for (int i = 0; i < suspects.size(); i++) {
                Suspect s = suspects.get(i);
                boolean isSelected = (i == selectedSuspectIndex);

                String details = String.format("Age: %d\nOccupation: %s\nAlibi: %s\nVerified: %s",
                        s.getAge(),
                        s.getOccupation().isEmpty() ? "Unknown" : s.getOccupation(),
                        s.getAlibi().isEmpty() ? "None recorded" : s.getAlibi(),
                        s.isAlibiVerified() ? "Yes" : "Pending");

                String imgKey = "suspect_arjun.jpg";
                cardsContainer.add(createSuspectCard(i, s, imgKey, s.getName().toUpperCase(),
                        "— " + s.getRole() + " —", details, s.getSuspicionScore(), isSelected));
            }
        } catch (Exception e) {
            JLabel errLbl = new JLabel("Database Error loading suspects: " + e.getMessage());
            errLbl.setForeground(new Color(220, 80, 80));
            errLbl.setFont(Theme.regular(13));
            cardsContainer.add(errLbl);
        }
    }

    private JPanel createSuspectCard(int index, Suspect s, String imgKey, String name, String role,
                                     String details, int suspicion, boolean isSelectedDefault) {
        GlassPanel card = new GlassPanel();
        card.setCornerRadius(14);
        card.setSelected(isSelectedDefault);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        int cardW = isSelectedDefault ? 280 : 220;
        int cardH = isSelectedDefault ? 480 : 420;
        card.setPreferredSize(new Dimension(cardW, cardH));
        card.setBorder(BorderFactory.createEmptyBorder(18, 16, 18, 16));
        suspectCards.add(card);

        // Suspect Portrait Image
        int avatarSize = isSelectedDefault ? 110 : 90;
        JPanel avatarPanel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(avatarSize + 10, avatarSize + 10);
            }

            @Override
            public Dimension getMaximumSize() {
                return new Dimension(avatarSize + 10, avatarSize + 10);
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.applyQualityHints(g2);

                int x = (getWidth() - avatarSize) / 2;
                int y = (getHeight() - avatarSize) / 2;

                BufferedImage img = AssetManager.getImage(imgKey);
                BufferedImage rounded = AssetManager.getRoundedImage(img, avatarSize, avatarSize, 12,
                        isSelectedDefault ? Theme.BORDER_GOLD : Theme.BORDER_CARD);
                g2.drawImage(rounded, x, y, null);

                g2.dispose();
            }
        };
        avatarPanel.setOpaque(false);
        avatarPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Name
        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(Theme.titleFont(isSelectedDefault ? 19 : 16));
        nameLbl.setForeground(isSelectedDefault ? Theme.TEXT_GOLD : Theme.TEXT_PRIMARY);
        nameLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Role / Title
        JLabel roleLbl = new JLabel(role);
        roleLbl.setFont(Theme.regular(12));
        roleLbl.setForeground(Theme.TEXT_MUTED);
        roleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Suspicion Stat Bar
        StatBar suspicionBar = new StatBar(suspicion, isSelectedDefault ? Theme.GOLD_PRIMARY : Theme.TEXT_MUTED);
        suspicionBar.setMaximumSize(new Dimension(240, 14));
        suspicionBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Details Panel
        JTextArea detailsArea = new JTextArea(details);
        detailsArea.setFont(Theme.regular(11));
        detailsArea.setForeground(Theme.TEXT_SECONDARY);
        detailsArea.setOpaque(false);
        detailsArea.setEditable(false);
        detailsArea.setFocusable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setMaximumSize(new Dimension(240, 90));
        detailsArea.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Action Button: Interrogate
        CustomButton btnInterrogate = new CustomButton(
                isSelectedDefault ? "INTERROGATE SUSPECT" : "VIEW PROFILE",
                isSelectedDefault ? CustomButton.ButtonStyle.GOLD : CustomButton.ButtonStyle.DARK
        );
        btnInterrogate.setMaximumSize(new Dimension(220, 36));
        btnInterrogate.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnInterrogate.addActionListener(e -> {
            ScreenManager.getInstance().showScreen(ScreenManager.INTERROGATION);
        });

        // Assemble Card
        card.add(avatarPanel);
        card.add(Box.createVerticalStrut(12));
        card.add(nameLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(roleLbl);
        card.add(Box.createVerticalStrut(14));
        card.add(suspicionBar);
        card.add(Box.createVerticalStrut(12));
        card.add(detailsArea);
        card.add(Box.createVerticalGlue());
        card.add(btnInterrogate);

        // Selection Listener
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedSuspectIndex = index;
                for (int i = 0; i < suspectCards.size(); i++) {
                    suspectCards.get(i).setSelected(i == index);
                }
            }
        });

        return card;
    }
}
