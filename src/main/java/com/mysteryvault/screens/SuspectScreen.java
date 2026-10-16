package com.mysteryvault.screens;

import com.mysteryvault.components.*;
import com.mysteryvault.navigation.ScreenManager;
import com.mysteryvault.theme.AssetManager;
import com.mysteryvault.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class SuspectScreen extends JPanel {

    private int selectedSuspectIndex = 1; // Meera selected by default
    private final List<GlassPanel> suspectCards = new ArrayList<>();

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

        JPanel cardsContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 0));
        cardsContainer.setOpaque(false);

        // Suspect 1: Arjun
        cardsContainer.add(createSuspectCard(0, "suspect_arjun.jpg", "ARJUN", "— Businessman —",
                "Age: 35\nStatus: Single\nLast Location: Bar Lounge\nKnown Connection: Business Partner",
                45, false));

        // Suspect 2: Meera (Selected / Active)
        cardsContainer.add(createSuspectCard(1, "suspect_meera.png", "MEERA", "— Accountant —",
                "Age: 32\nStatus: In Relationship\nLast Location: Restaurant\nKnown Connection: Financial Auditor",
                72, true));

        // Suspect 3: Rohan
        cardsContainer.add(createSuspectCard(2, "suspect_rohan.png", "ROHAN", "— Chef —",
                "Age: 29\nStatus: Married\nLast Location: Kitchen\nKnown Connection: Head Chef",
                31, false));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        centerWrapper.add(cardsContainer, gbc);

        add(centerWrapper, BorderLayout.CENTER);
    }

    private JPanel createSuspectCard(int index, String imgKey, String name, String role,
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

        // Suspicion bar & label
        JPanel suspicionPanel = new JPanel(new BorderLayout(6, 4));
        suspicionPanel.setOpaque(false);
        suspicionPanel.setMaximumSize(new Dimension(200, 32));
        suspicionPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel suspLbl = new JLabel("Suspicion: " + suspicion + "%");
        suspLbl.setFont(Theme.bold(11));
        suspLbl.setForeground(suspicion > 60 ? Theme.RED_CONFLICT : Theme.GOLD_PRIMARY);

        Color barColor = suspicion > 60 ? Theme.RED_CONFLICT : Theme.GOLD_PRIMARY;
        StatBar suspBar = new StatBar(suspicion, barColor);
        suspBar.setPreferredSize(new Dimension(180, 7));

        suspicionPanel.add(suspLbl, BorderLayout.NORTH);
        suspicionPanel.add(suspBar, BorderLayout.SOUTH);

        card.add(avatarPanel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(nameLbl);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(roleLbl);
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(suspicionPanel);

        // Extra details for selected card (e.g. Meera)
        if (isSelectedDefault) {
            card.add(Box.createRigidArea(new Dimension(0, 14)));

            JPanel detailsBox = new JPanel(new GridLayout(4, 1, 0, 4));
            detailsBox.setOpaque(false);
            detailsBox.setMaximumSize(new Dimension(240, 90));

            String[] lines = details.split("\n");
            for (String line : lines) {
                JLabel lineLbl = new JLabel(line);
                lineLbl.setFont(Theme.regular(11));
                lineLbl.setForeground(Theme.TEXT_SECONDARY);
                detailsBox.add(lineLbl);
            }
            card.add(detailsBox);

            card.add(Box.createRigidArea(new Dimension(0, 16)));

            // Action buttons
            JPanel actionBtns = new JPanel(new GridLayout(1, 2, 8, 0));
            actionBtns.setOpaque(false);
            actionBtns.setMaximumSize(new Dimension(250, 36));

            CustomButton btnEvidence = new CustomButton("VIEW EVIDENCE", CustomButton.ButtonStyle.DARK);
            btnEvidence.addActionListener(e -> ScreenManager.getInstance().showScreen(ScreenManager.EVIDENCE_INVESTIGATION));

            CustomButton btnInterrogate = new CustomButton("INTERROGATE", CustomButton.ButtonStyle.GOLD);
            btnInterrogate.addActionListener(e -> ScreenManager.getInstance().showScreen(ScreenManager.INTERROGATION));

            actionBtns.add(btnEvidence);
            actionBtns.add(btnInterrogate);
            card.add(actionBtns);
        }

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedSuspectIndex = index;
                // Highlight clicked card
                for (int i = 0; i < suspectCards.size(); i++) {
                    suspectCards.get(i).setSelected(i == index);
                }
            }
        });

        return card;
    }
}
