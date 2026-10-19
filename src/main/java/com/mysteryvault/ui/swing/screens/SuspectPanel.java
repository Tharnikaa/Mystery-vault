package com.mysteryvault.ui.swing.screens;

import com.mysteryvault.ui.swing.components.*;
import com.mysteryvault.ui.swing.mock.MockSuspectViewPort;
import com.mysteryvault.ui.swing.navigation.ScreenManager;
import com.mysteryvault.ui.swing.ports.SuspectViewPort;
import com.mysteryvault.ui.swing.theme.AssetManager;
import com.mysteryvault.ui.swing.theme.Theme;
import com.mysteryvault.ui.swing.viewmodel.SuspectViewModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class SuspectPanel extends JPanel {

    private final SuspectViewPort suspectPort;
    private int selectedSuspectIndex = 1; // Meera selected by default
    private final List<GlassPanel> suspectCards = new ArrayList<>();

    public SuspectPanel(SuspectViewPort suspectPort) {
        this.suspectPort = suspectPort != null ? suspectPort : new MockSuspectViewPort();
        setOpaque(true);
        setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());

        initUI();
    }

    public SuspectPanel() {
        this(new MockSuspectViewPort());
    }

    private void initUI() {
        ScreenHeader header = new ScreenHeader("5. SUSPECT SCREEN");
        add(header, BorderLayout.NORTH);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        JPanel cardsContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 0));
        cardsContainer.setOpaque(false);

        List<SuspectViewModel> suspects = suspectPort.getSuspectsForCase("CASE_103");
        for (int i = 0; i < suspects.size(); i++) {
            SuspectViewModel s = suspects.get(i);
            cardsContainer.add(createSuspectCard(i, s));
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        centerWrapper.add(cardsContainer, gbc);

        add(centerWrapper, BorderLayout.CENTER);
    }

    private JPanel createSuspectCard(int index, SuspectViewModel suspect) {
        boolean isSelectedDefault = suspect.isDefaultExpanded();
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

                BufferedImage img = AssetManager.getImage(suspect.getImageKey());
                BufferedImage rounded = AssetManager.getRoundedImage(img, avatarSize, avatarSize, 12,
                        isSelectedDefault ? Theme.BORDER_GOLD : Theme.BORDER_CARD);
                g2.drawImage(rounded, x, y, null);

                g2.dispose();
            }
        };
        avatarPanel.setOpaque(false);
        avatarPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLbl = new JLabel(suspect.getName());
        nameLbl.setFont(Theme.titleFont(isSelectedDefault ? 19 : 16));
        nameLbl.setForeground(isSelectedDefault ? Theme.TEXT_GOLD : Theme.TEXT_PRIMARY);
        nameLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel roleLbl = new JLabel(suspect.getRole());
        roleLbl.setFont(Theme.regular(12));
        roleLbl.setForeground(Theme.TEXT_MUTED);
        roleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel suspicionPanel = new JPanel(new BorderLayout(6, 4));
        suspicionPanel.setOpaque(false);
        suspicionPanel.setMaximumSize(new Dimension(200, 32));
        suspicionPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel suspLbl = new JLabel("Suspicion: " + suspect.getSuspicion() + "%");
        suspLbl.setFont(Theme.bold(11));
        suspLbl.setForeground(suspect.getSuspicion() > 60 ? Theme.RED_CONFLICT : Theme.GOLD_PRIMARY);

        Color barColor = suspect.getSuspicion() > 60 ? Theme.RED_CONFLICT : Theme.GOLD_PRIMARY;
        StatBar suspBar = new StatBar(suspect.getSuspicion(), barColor);
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

        if (isSelectedDefault) {
            card.add(Box.createRigidArea(new Dimension(0, 14)));

            JPanel detailsBox = new JPanel(new GridLayout(4, 1, 0, 4));
            detailsBox.setOpaque(false);
            detailsBox.setMaximumSize(new Dimension(240, 90));

            detailsBox.add(new JLabel("Age: " + suspect.getAge()));
            detailsBox.add(new JLabel("Status: " + suspect.getStatus()));
            detailsBox.add(new JLabel("Last Location: " + suspect.getLastLocation()));
            detailsBox.add(new JLabel("Known Connection: " + suspect.getKnownConnection()));

            for (Component c : detailsBox.getComponents()) {
                c.setFont(Theme.regular(11));
                c.setForeground(Theme.TEXT_SECONDARY);
            }

            card.add(detailsBox);
            card.add(Box.createRigidArea(new Dimension(0, 16)));

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
                for (int i = 0; i < suspectCards.size(); i++) {
                    suspectCards.get(i).setSelected(i == index);
                }
            }
        });

        return card;
    }
}
