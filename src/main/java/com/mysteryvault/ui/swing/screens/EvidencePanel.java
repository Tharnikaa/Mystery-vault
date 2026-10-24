package com.mysteryvault.ui.swing.screens;

import com.mysteryvault.ui.swing.components.*;
import com.mysteryvault.ui.swing.mock.MockEvidenceViewPort;
import com.mysteryvault.ui.swing.navigation.ScreenManager;
import com.mysteryvault.ui.swing.ports.EvidenceViewPort;
import com.mysteryvault.ui.swing.theme.AssetManager;
import com.mysteryvault.ui.swing.theme.Theme;
import com.mysteryvault.ui.swing.viewmodel.EvidenceViewModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class EvidencePanel extends JPanel implements com.mysteryvault.ui.swing.navigation.RefreshableScreen {

    private final EvidenceViewPort evidencePort;
    private final List<EvidenceViewModel> evidenceList = new ArrayList<>();
    private int selectedIndex = 0;

    private JLabel lblId;
    private JLabel lblType;
    private JLabel lblLocation;
    private JLabel lblTime;
    private JLabel lblDescription;
    private JPanel photoPanel;
    private JPanel leftListPanel;
    private final List<GlassPanel> listItemPanels = new ArrayList<>();

    public EvidencePanel(EvidenceViewPort evidencePort) {
        this.evidencePort = evidencePort != null ? evidencePort : new MockEvidenceViewPort();
        setOpaque(true);
        setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());

        this.evidenceList.addAll(this.evidencePort.getEvidenceForCase("CASE_101"));
        initUI();
    }

    public EvidencePanel() {
        this(new MockEvidenceViewPort());
    }

    @Override
    public void refreshScreen(String caseId) {
        evidenceList.clear();
        evidenceList.addAll(evidencePort.getEvidenceForCase(caseId));
        if (leftListPanel != null) {
            leftListPanel.removeAll();
            listItemPanels.clear();
            selectedIndex = 0;
            for (int i = 0; i < evidenceList.size(); i++) {
                EvidenceViewModel item = evidenceList.get(i);
                int idx = i;
                GlassPanel itemCard = new GlassPanel(new FlowLayout(FlowLayout.LEFT, 14, 12));
                itemCard.setCornerRadius(10);
                itemCard.setSelected(i == selectedIndex);
                itemCard.setCursor(new Cursor(Cursor.HAND_CURSOR));
                itemCard.setMaximumSize(new Dimension(270, 48));

                JLabel titleLbl = new JLabel(item.getTitle());
                titleLbl.setFont(Theme.bold(13));
                titleLbl.setForeground(i == selectedIndex ? Theme.TEXT_GOLD : Theme.TEXT_PRIMARY);

                itemCard.add(titleLbl);
                listItemPanels.add(itemCard);

                itemCard.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        selectEvidence(idx);
                    }
                });
                leftListPanel.add(itemCard);
                leftListPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            }
            leftListPanel.revalidate();
            leftListPanel.repaint();
            if (!evidenceList.isEmpty()) {
                selectEvidence(0);
            }
        }
    }

    private void initUI() {
        ScreenHeader header = new ScreenHeader("6. EVIDENCE INVESTIGATION");
        add(header, BorderLayout.NORTH);

        JPanel mainSplit = new JPanel(new BorderLayout(20, 0));
        mainSplit.setOpaque(false);
        mainSplit.setBorder(BorderFactory.createEmptyBorder(10, 40, 20, 40));

        leftListPanel = new JPanel();
        leftListPanel.setLayout(new BoxLayout(leftListPanel, BoxLayout.Y_AXIS));
        leftListPanel.setOpaque(false);
        leftListPanel.setPreferredSize(new Dimension(280, 520));

        for (int i = 0; i < evidenceList.size(); i++) {
            EvidenceViewModel item = evidenceList.get(i);
            int idx = i;

            GlassPanel itemCard = new GlassPanel(new FlowLayout(FlowLayout.LEFT, 14, 12));
            itemCard.setCornerRadius(10);
            itemCard.setSelected(i == selectedIndex);
            itemCard.setCursor(new Cursor(Cursor.HAND_CURSOR));
            itemCard.setMaximumSize(new Dimension(270, 48));

            JLabel titleLbl = new JLabel(item.getTitle());
            titleLbl.setFont(Theme.bold(13));
            titleLbl.setForeground(i == selectedIndex ? Theme.TEXT_GOLD : Theme.TEXT_PRIMARY);

            itemCard.add(titleLbl);
            listItemPanels.add(itemCard);

            itemCard.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectEvidence(idx);
                }
            });

            leftListPanel.add(itemCard);
            leftListPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        mainSplit.add(leftListPanel, BorderLayout.WEST);

        GlassPanel rightDetailCard = new GlassPanel(new BorderLayout(0, 14));
        rightDetailCard.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        photoPanel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(500, 220);
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.applyQualityHints(g2);

                int w = getWidth();
                int h = getHeight();

                EvidenceViewModel item = evidenceList.get(selectedIndex);
                BufferedImage img = AssetManager.getImage(item.getImageKey());
                BufferedImage rounded = AssetManager.getRoundedImage(img, w, h, 10, Theme.BORDER_CARD);
                g2.drawImage(rounded, 0, 0, null);

                g2.setColor(new Color(212, 163, 89, 40));
                g2.drawRect(20, 20, 40, 40);
                g2.drawString("EVID # " + item.getId(), 24, 45);

                g2.dispose();
            }
        };
        photoPanel.setOpaque(false);
        rightDetailCard.add(photoPanel, BorderLayout.NORTH);

        JPanel metadataPanel = new JPanel();
        metadataPanel.setLayout(new BoxLayout(metadataPanel, BoxLayout.Y_AXIS));
        metadataPanel.setOpaque(false);

        lblId = new JLabel("Evidence ID: E01");
        lblId.setFont(Theme.bold(13));
        lblId.setForeground(Theme.TEXT_GOLD);

        lblType = new JLabel("Evidence Type: Physical Evidence");
        lblType.setFont(Theme.regular(12));
        lblType.setForeground(Theme.TEXT_PRIMARY);

        lblLocation = new JLabel("Location: Restaurant VIP Table");
        lblLocation.setFont(Theme.regular(12));
        lblLocation.setForeground(Theme.TEXT_SECONDARY);

        lblTime = new JLabel("Time Collected: 09:45 PM");
        lblTime.setFont(Theme.regular(12));
        lblTime.setForeground(Theme.TEXT_SECONDARY);

        lblDescription = new JLabel("<html><b>Description:</b> Traces of an unidentified powder detected on the rim of the glass.</html>");
        lblDescription.setFont(Theme.regular(12));
        lblDescription.setForeground(Theme.TEXT_PRIMARY);

        metadataPanel.add(lblId);
        metadataPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        metadataPanel.add(lblType);
        metadataPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        metadataPanel.add(lblLocation);
        metadataPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        metadataPanel.add(lblTime);
        metadataPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        metadataPanel.add(lblDescription);

        rightDetailCard.add(metadataPanel, BorderLayout.CENTER);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionRow.setOpaque(false);

        CustomButton btnExamine = new CustomButton("EXAMINE", CustomButton.ButtonStyle.DARK);
        btnExamine.addActionListener(e -> {
            String res = evidencePort.examineEvidence(evidenceList.get(selectedIndex).getId());
            JOptionPane.showMessageDialog(this, res, "Forensic Examination", JOptionPane.INFORMATION_MESSAGE);
        });

        CustomButton btnLab = new CustomButton("SEND TO LAB", CustomButton.ButtonStyle.DARK);
        btnLab.addActionListener(e -> {
            String res = evidencePort.sendToLab(evidenceList.get(selectedIndex).getId());
            JOptionPane.showMessageDialog(this, res, "Forensic Lab", JOptionPane.INFORMATION_MESSAGE);
        });

        CustomButton btnLink = new CustomButton("LINK TO SUSPECT", CustomButton.ButtonStyle.GOLD);
        btnLink.addActionListener(e -> ScreenManager.getInstance().showScreen(ScreenManager.CASE_CONNECTION_ENGINE));

        actionRow.add(btnExamine);
        actionRow.add(btnLab);
        actionRow.add(btnLink);

        rightDetailCard.add(actionRow, BorderLayout.SOUTH);

        mainSplit.add(rightDetailCard, BorderLayout.CENTER);
        add(mainSplit, BorderLayout.CENTER);

        selectEvidence(0);
    }

    private void selectEvidence(int index) {
        this.selectedIndex = index;
        EvidenceViewModel item = evidenceList.get(index);

        lblId.setText("Evidence ID: " + item.getId());
        lblType.setText("Evidence Type: " + item.getType());
        lblLocation.setText("Location: " + item.getLocation());
        lblTime.setText("Time Collected: " + item.getTimeCollected());
        lblDescription.setText("<html><b>Description:</b> " + item.getDescription() + "</html>");

        for (int i = 0; i < listItemPanels.size(); i++) {
            GlassPanel p = listItemPanels.get(i);
            p.setSelected(i == index);
            JLabel lbl = (JLabel) p.getComponent(0);
            lbl.setForeground(i == index ? Theme.TEXT_GOLD : Theme.TEXT_PRIMARY);
        }

        photoPanel.repaint();
    }

    public void selectEvidenceById(String evidenceId) {
        if (evidenceId == null) return;
        for (int i = 0; i < evidenceList.size(); i++) {
            if (evidenceList.get(i).getId().equalsIgnoreCase(evidenceId)) {
                selectEvidence(i);
                return;
            }
        }
    }

    public void filterOrSelectSuspectEvidence(String suspectId) {
        if (suspectId == null) return;
        for (int i = 0; i < evidenceList.size(); i++) {
            String assoc = evidenceList.get(i).getAssociatedPersonId();
            if (assoc != null && (assoc.equalsIgnoreCase(suspectId) || assoc.contains(suspectId))) {
                selectEvidence(i);
                return;
            }
        }
    }

    public List<EvidenceViewModel> getEvidenceList() {
        return java.util.Collections.unmodifiableList(evidenceList);
    }
}
