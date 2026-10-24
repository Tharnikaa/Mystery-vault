package com.mysteryvault.ui.swing.screens;

import com.mysteryvault.ui.swing.components.*;
import com.mysteryvault.ui.swing.mock.MockEvidenceViewPort;
import com.mysteryvault.ui.swing.mock.MockSuspectViewPort;
import com.mysteryvault.ui.swing.navigation.RefreshableScreen;
import com.mysteryvault.ui.swing.navigation.ScreenManager;
import com.mysteryvault.ui.swing.ports.EvidenceViewPort;
import com.mysteryvault.ui.swing.ports.SuspectViewPort;
import com.mysteryvault.ui.swing.theme.AssetManager;
import com.mysteryvault.ui.swing.theme.Theme;
import com.mysteryvault.ui.swing.viewmodel.EvidenceViewModel;
import com.mysteryvault.ui.swing.viewmodel.SuspectViewModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SuspectPanel extends JPanel implements RefreshableScreen {

    private final SuspectViewPort suspectPort;
    private final EvidenceViewPort evidencePort;
    private int selectedSuspectIndex = 0;
    private final List<GlassPanel> suspectCards = new ArrayList<>();
    private final List<SuspectViewModel> suspectsList = new ArrayList<>();
    private JPanel cardsContainer;
    private String activeCaseId = "CASE_101";

    public SuspectPanel(SuspectViewPort suspectPort, EvidenceViewPort evidencePort) {
        this.suspectPort = suspectPort != null ? suspectPort : new MockSuspectViewPort();
        this.evidencePort = evidencePort != null ? evidencePort : new MockEvidenceViewPort();

        setOpaque(true);
        setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());

        initUI();
    }

    public SuspectPanel(SuspectViewPort suspectPort) {
        this(suspectPort, new MockEvidenceViewPort());
    }

    public SuspectPanel() {
        this(new MockSuspectViewPort(), new MockEvidenceViewPort());
    }

    @Override
    public void refreshScreen(String caseId) {
        this.activeCaseId = caseId != null ? caseId : "CASE_101";
        if (cardsContainer != null) {
            cardsContainer.removeAll();
            suspectCards.clear();
            suspectsList.clear();
            selectedSuspectIndex = 0;

            List<SuspectViewModel> suspects = suspectPort.getSuspectsForCase(this.activeCaseId);
            suspectsList.addAll(suspects);

            for (int i = 0; i < suspects.size(); i++) {
                SuspectViewModel s = suspects.get(i);
                cardsContainer.add(createSuspectCard(i, s));
            }
            cardsContainer.revalidate();
            cardsContainer.repaint();
        }
    }

    public List<GlassPanel> getSuspectCards() {
        return Collections.unmodifiableList(suspectCards);
    }

    public List<SuspectViewModel> getSuspectsList() {
        return Collections.unmodifiableList(suspectsList);
    }

    private void initUI() {
        ScreenHeader header = new ScreenHeader("5. SUSPECT SCREEN");
        add(header, BorderLayout.NORTH);

        cardsContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        cardsContainer.setOpaque(false);

        List<SuspectViewModel> suspects = suspectPort.getSuspectsForCase(this.activeCaseId);
        suspectsList.addAll(suspects);

        for (int i = 0; i < suspects.size(); i++) {
            SuspectViewModel s = suspects.get(i);
            cardsContainer.add(createSuspectCard(i, s));
        }

        JScrollPane scrollPane = new JScrollPane(cardsContainer);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(24);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Creates a consistent, fully actionable suspect card.
     * Every suspect card has VIEW DETAILS, VIEW EVIDENCE, and INTERROGATE.
     */
    private JPanel createSuspectCard(int index, SuspectViewModel suspect) {
        boolean isSelected = (index == selectedSuspectIndex);

        GlassPanel card = new GlassPanel();
        card.setCornerRadius(14);
        card.setSelected(isSelected);
        card.setBorderColor(Theme.BORDER_GOLD);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // Fixed dimension to maintain uniform alignment across all cards
        card.setPreferredSize(new Dimension(245, 490));
        card.setMinimumSize(new Dimension(245, 490));
        card.setMaximumSize(new Dimension(245, 490));
        card.setBorder(BorderFactory.createEmptyBorder(16, 14, 16, 14));
        suspectCards.add(card);

        // Avatar
        int avatarSize = 95;
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
                BufferedImage rounded = AssetManager.getRoundedImage(img, avatarSize, avatarSize, 12, Theme.BORDER_GOLD);
                g2.drawImage(rounded, x, y, null);

                g2.dispose();
            }
        };
        avatarPanel.setOpaque(false);
        avatarPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Name & Role
        JLabel nameLbl = new JLabel(suspect.getName(), SwingConstants.CENTER);
        nameLbl.setFont(Theme.titleFont(16));
        nameLbl.setForeground(Theme.TEXT_GOLD);
        nameLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel roleLbl = new JLabel(suspect.getRole(), SwingConstants.CENTER);
        roleLbl.setFont(Theme.bold(12));
        roleLbl.setForeground(Theme.TEXT_PRIMARY);
        roleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel occLbl = new JLabel(suspect.getOccupation(), SwingConstants.CENTER);
        occLbl.setFont(Theme.regular(11));
        occLbl.setForeground(Theme.TEXT_MUTED);
        occLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Suspicion Stat Bar
        JPanel suspicionPanel = new JPanel(new BorderLayout(6, 4));
        suspicionPanel.setOpaque(false);
        suspicionPanel.setMaximumSize(new Dimension(215, 32));
        suspicionPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel suspLbl = new JLabel("Suspicion: " + suspect.getSuspicion() + "%");
        suspLbl.setFont(Theme.bold(11));
        Color suspColor = suspect.getSuspicion() > 60 ? Theme.RED_CONFLICT : Theme.GOLD_PRIMARY;
        suspLbl.setForeground(suspColor);

        StatBar suspBar = new StatBar(suspect.getSuspicion(), suspColor);
        suspBar.setPreferredSize(new Dimension(200, 6));

        suspicionPanel.add(suspLbl, BorderLayout.NORTH);
        suspicionPanel.add(suspBar, BorderLayout.SOUTH);

        // Trust Stat Bar
        JPanel trustPanel = new JPanel(new BorderLayout(6, 4));
        trustPanel.setOpaque(false);
        trustPanel.setMaximumSize(new Dimension(215, 32));
        trustPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel trustLbl = new JLabel("Trust: " + suspect.getTrust() + "%");
        trustLbl.setFont(Theme.bold(11));
        trustLbl.setForeground(Theme.TEAL_ACCENT);

        StatBar trustBar = new StatBar(suspect.getTrust(), Theme.TEAL_ACCENT);
        trustBar.setPreferredSize(new Dimension(200, 6));

        trustPanel.add(trustLbl, BorderLayout.NORTH);
        trustPanel.add(trustBar, BorderLayout.SOUTH);

        // Status Badge
        JLabel statusBadge = new JLabel(suspect.getStatus(), SwingConstants.CENTER);
        statusBadge.setFont(Theme.regular(11));
        statusBadge.setForeground(Theme.TEXT_SECONDARY);
        statusBadge.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Action Buttons Row (EVERY suspect card has VIEW DETAILS, VIEW EVIDENCE, INTERROGATE)
        JPanel actionBtns = new JPanel(new GridLayout(3, 1, 0, 7));
        actionBtns.setOpaque(false);
        actionBtns.setMaximumSize(new Dimension(215, 115));
        actionBtns.setAlignmentX(Component.CENTER_ALIGNMENT);

        CustomButton btnDetails = new CustomButton("VIEW DETAILS", CustomButton.ButtonStyle.DARK);
        btnDetails.setName("btnDetails_" + suspect.getId());
        btnDetails.setFont(Theme.bold(11));
        btnDetails.addActionListener(e -> showSuspectDetailsDialog(suspect));

        CustomButton btnEvidence = new CustomButton("VIEW EVIDENCE", CustomButton.ButtonStyle.DARK);
        btnEvidence.setName("btnEvidence_" + suspect.getId());
        btnEvidence.setFont(Theme.bold(11));
        btnEvidence.addActionListener(e -> showSuspectEvidenceDialog(suspect));

        CustomButton btnInterrogate = new CustomButton("INTERROGATE", CustomButton.ButtonStyle.GOLD);
        btnInterrogate.setName("btnInterrogate_" + suspect.getId());
        btnInterrogate.setFont(Theme.bold(11));
        btnInterrogate.addActionListener(e -> ScreenManager.getInstance().interrogateSuspect(suspect.getId()));

        actionBtns.add(btnDetails);
        actionBtns.add(btnEvidence);
        actionBtns.add(btnInterrogate);

        // Assemble Card
        card.add(avatarPanel);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(nameLbl);
        card.add(Box.createRigidArea(new Dimension(0, 2)));
        card.add(roleLbl);
        card.add(occLbl);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(suspicionPanel);
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(trustPanel);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(statusBadge);
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(actionBtns);

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

    /**
     * Displays suspect's full investigation profile without revealing hidden culprit spoilers.
     */
    public void showSuspectDetailsDialog(SuspectViewModel s) {
        Window parent = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parent instanceof Frame ? (Frame) parent : null, "Suspect Profile: " + s.getName(), true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Theme.BG_DARK);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel(s.getName().toUpperCase());
        title.setFont(Theme.titleFont(18));
        title.setForeground(Theme.TEXT_GOLD);
        content.add(title);
        content.add(Box.createRigidArea(new Dimension(0, 12)));

        JPanel grid = new JPanel(new GridLayout(6, 2, 12, 10));
        grid.setOpaque(false);

        addDetailRow(grid, "Role / Title:", s.getRole());
        addDetailRow(grid, "Occupation:", s.getOccupation());
        addDetailRow(grid, "Age:", s.getAge() > 0 ? String.valueOf(s.getAge()) : "Undisclosed");
        addDetailRow(grid, "Status:", s.getStatus());
        addDetailRow(grid, "Last Location:", s.getLastLocation());
        addDetailRow(grid, "Known Connections:", s.getKnownConnection());

        content.add(grid);
        content.add(Box.createRigidArea(new Dimension(0, 16)));

        JLabel alibiTitle = new JLabel("Official Alibi Statement:");
        alibiTitle.setFont(Theme.bold(12));
        alibiTitle.setForeground(Theme.TEXT_GOLD);
        content.add(alibiTitle);
        content.add(Box.createRigidArea(new Dimension(0, 4)));

        JTextArea alibiText = new JTextArea(s.getAlibi());
        alibiText.setFont(Theme.regular(12));
        alibiText.setForeground(Theme.TEXT_PRIMARY);
        alibiText.setBackground(new Color(22, 29, 44));
        alibiText.setLineWrap(true);
        alibiText.setWrapStyleWord(true);
        alibiText.setEditable(false);
        alibiText.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_CARD, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        content.add(alibiText);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        bottom.setOpaque(false);

        CustomButton btnInterrogate = new CustomButton("INTERROGATE NOW", CustomButton.ButtonStyle.GOLD);
        btnInterrogate.addActionListener(e -> {
            dialog.dispose();
            ScreenManager.getInstance().interrogateSuspect(s.getId());
        });

        CustomButton btnClose = new CustomButton("CLOSE", CustomButton.ButtonStyle.DARK);
        btnClose.addActionListener(e -> dialog.dispose());

        bottom.add(btnInterrogate);
        bottom.add(btnClose);

        dialog.add(content, BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.setSize(520, 450);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private void addDetailRow(JPanel grid, String label, String value) {
        JLabel l = new JLabel(label);
        l.setFont(Theme.bold(12));
        l.setForeground(Theme.TEXT_SECONDARY);

        JLabel v = new JLabel(value != null && !value.isEmpty() ? value : "None recorded");
        v.setFont(Theme.regular(12));
        v.setForeground(Theme.TEXT_PRIMARY);

        grid.add(l);
        grid.add(v);
    }

    /**
     * Displays evidence genuinely associated with this suspect.
     */
    public void showSuspectEvidenceDialog(SuspectViewModel s) {
        Window parent = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parent instanceof Frame ? (Frame) parent : null, "Associated Evidence: " + s.getName(), true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Theme.BG_DARK);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JLabel title = new JLabel("FORENSIC EVIDENCE TIED TO " + s.getName().toUpperCase());
        title.setFont(Theme.titleFont(16));
        title.setForeground(Theme.TEXT_GOLD);
        content.add(title);
        content.add(Box.createRigidArea(new Dimension(0, 12)));

        List<EvidenceViewModel> linkedEvidence = evidencePort.getEvidenceForSuspect(this.activeCaseId, s.getId());

        if (linkedEvidence.isEmpty()) {
            JLabel emptyLbl = new JLabel("<html><p style='width: 440px;'>No forensic or physical evidence has been cataloged linking directly to " + s.getName() + " in current logs.</p></html>");
            emptyLbl.setFont(Theme.regular(13));
            emptyLbl.setForeground(Theme.TEXT_MUTED);
            content.add(emptyLbl);
        } else {
            JPanel listPanel = new JPanel();
            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
            listPanel.setOpaque(false);

            for (EvidenceViewModel ev : linkedEvidence) {
                GlassPanel evCard = new GlassPanel(new BorderLayout(8, 6));
                evCard.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
                evCard.setMaximumSize(new Dimension(500, 95));
                evCard.setBorderColor(Theme.BORDER_GOLD);

                JPanel leftInfo = new JPanel(new GridLayout(3, 1, 0, 2));
                leftInfo.setOpaque(false);

                JLabel evTitle = new JLabel(ev.getId() + " • " + ev.getTitle());
                evTitle.setFont(Theme.bold(13));
                evTitle.setForeground(Theme.TEXT_GOLD);

                JLabel evMeta = new JLabel("Type: " + ev.getType() + " • Relevance: " + ev.getSignificance() + " • " + (ev.isDiscovered() ? "Discovered" : "Undiscovered"));
                evMeta.setFont(Theme.regular(11));
                evMeta.setForeground(Theme.TEXT_SECONDARY);

                JLabel evDesc = new JLabel("<html><p style='width: 320px;'>" + ev.getDescription() + "</p></html>");
                evDesc.setFont(Theme.regular(11));
                evDesc.setForeground(Theme.TEXT_PRIMARY);

                leftInfo.add(evTitle);
                leftInfo.add(evMeta);
                leftInfo.add(evDesc);

                CustomButton btnInspect = new CustomButton("INSPECT", CustomButton.ButtonStyle.GOLD);
                btnInspect.setPreferredSize(new Dimension(95, 32));
                btnInspect.addActionListener(e -> {
                    dialog.dispose();
                    ScreenManager.getInstance().selectEvidence(ev.getId());
                });

                evCard.add(leftInfo, BorderLayout.CENTER);
                evCard.add(btnInspect, BorderLayout.EAST);

                listPanel.add(evCard);
                listPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            }

            JScrollPane sp = new JScrollPane(listPanel);
            sp.setOpaque(false);
            sp.getViewport().setOpaque(false);
            sp.setBorder(null);
            sp.setPreferredSize(new Dimension(500, 280));
            content.add(sp);
        }

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setOpaque(false);

        CustomButton btnClose = new CustomButton("CLOSE", CustomButton.ButtonStyle.DARK);
        btnClose.addActionListener(e -> dialog.dispose());
        bottom.add(btnClose);

        dialog.add(content, BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.setSize(560, 440);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }
}
