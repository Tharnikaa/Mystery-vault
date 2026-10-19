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

public class EvidenceScreen extends JPanel {

    private static class EvidenceItem {
        String id;
        String title;
        String type;
        String location;
        String time;
        String description;
        String imageKey;

        EvidenceItem(String id, String title, String type, String location, String time, String description, String imageKey) {
            this.id = id;
            this.title = title;
            this.type = type;
            this.location = location;
            this.time = time;
            this.description = description;
            this.imageKey = imageKey;
        }
    }

    private final List<EvidenceItem> evidenceList = new ArrayList<>();
    private int selectedIndex = 0;

    private JLabel lblId;
    private JLabel lblType;
    private JLabel lblLocation;
    private JLabel lblTime;
    private JLabel lblDescription;
    private JPanel photoPanel;
    private final List<GlassPanel> listItemPanels = new ArrayList<>();

    public EvidenceScreen() {
        setOpaque(true);
        setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());

        initData();
        initUI();
    }

    private void initData() {
        evidenceList.clear();
        String caseId = ScreenManager.getInstance().getSelectedCaseId();
        com.mysteryvault.service.EvidenceService service = ScreenManager.getInstance().getEvidenceService();

        try {
            List<com.mysteryvault.model.Evidence> list = service.listCanonicalEvidenceByCase(caseId);
            if (list.isEmpty() && "CASE-103".equalsIgnoreCase(caseId)) {
                for (com.mysteryvault.model.Evidence e : com.mysteryvault.service.SampleDataFactory.createSampleEvidence()) {
                    service.createEvidence(e);
                }
                list = service.listCanonicalEvidenceByCase(caseId);
            }

            for (com.mysteryvault.model.Evidence e : list) {
                String typeStr = (e instanceof com.mysteryvault.model.DocumentEvidence doc)
                        ? "Document (" + (doc.getDocumentType().isEmpty() ? "Document" : doc.getDocumentType()) + ")"
                        : (e instanceof com.mysteryvault.model.DigitalEvidence dig)
                        ? "Digital (" + (dig.getDeviceType().isEmpty() ? "Forensic" : dig.getDeviceType()) + ")"
                        : "Investigation Asset";

                String timeStr = e.getDiscoveredAt() != null
                        ? e.getDiscoveredAt().getHour() + ":" + String.format("%02d", e.getDiscoveredAt().getMinute())
                        : "N/A";

                String img = (e instanceof com.mysteryvault.model.DocumentEvidence) ? "case_diamond.png" : "case_poison.png";
                evidenceList.add(new EvidenceItem(
                        e.getId(),
                        e.getId() + " – " + e.getName(),
                        typeStr,
                        e.getLocationFound().isEmpty() ? "Unknown Location" : e.getLocationFound(),
                        timeStr,
                        e.getDescription(),
                        img
                ));
            }
        } catch (Exception err) {
            // Fallback for offline UI preview
            evidenceList.add(new EvidenceItem(
                    "E01", "E01 – Wine Glass", "Physical Evidence", "VIP Table", "09:45 PM",
                    "Traces of toxic alkaloid detected on the rim.", "evidence_wineglass.png"
            ));
        }

        if (evidenceList.isEmpty()) {
            evidenceList.add(new EvidenceItem(
                    "NONE", "No Evidence Cataloged", "Info", "N/A", "N/A",
                    "No evidence records cataloged for this case.", "case_diamond.png"
            ));
        }
    }

    private void initUI() {
        ScreenHeader header = new ScreenHeader("6. EVIDENCE INVESTIGATION");
        add(header, BorderLayout.NORTH);

        JPanel mainSplit = new JPanel(new BorderLayout(20, 0));
        mainSplit.setOpaque(false);
        mainSplit.setBorder(BorderFactory.createEmptyBorder(10, 40, 20, 40));

        // Left Sidebar: Evidence List
        JPanel leftListPanel = new JPanel();
        leftListPanel.setLayout(new BoxLayout(leftListPanel, BoxLayout.Y_AXIS));
        leftListPanel.setOpaque(false);
        leftListPanel.setPreferredSize(new Dimension(280, 520));

        for (int i = 0; i < evidenceList.size(); i++) {
            EvidenceItem item = evidenceList.get(i);
            int idx = i;

            GlassPanel itemCard = new GlassPanel(new FlowLayout(FlowLayout.LEFT, 14, 12));
            itemCard.setCornerRadius(10);
            itemCard.setSelected(i == selectedIndex);
            itemCard.setCursor(new Cursor(Cursor.HAND_CURSOR));
            itemCard.setMaximumSize(new Dimension(270, 48));

            JLabel titleLbl = new JLabel(item.title);
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

        // Right Panel: Evidence Details & Inspection Photo
        GlassPanel rightDetailCard = new GlassPanel(new BorderLayout(0, 14));
        rightDetailCard.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        // Photo Panel
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

                EvidenceItem item = evidenceList.get(selectedIndex);
                BufferedImage img = AssetManager.getImage(item.imageKey);
                BufferedImage rounded = AssetManager.getRoundedImage(img, w, h, 10, Theme.BORDER_CARD);
                g2.drawImage(rounded, 0, 0, null);

                // Subtle crime scene grid watermark
                g2.setColor(new Color(212, 163, 89, 40));
                g2.drawRect(20, 20, 40, 40);
                g2.drawString("EVID # " + item.id, 24, 45);

                g2.dispose();
            }
        };
        photoPanel.setOpaque(false);
        rightDetailCard.add(photoPanel, BorderLayout.NORTH);

        // Metadata panel below photo
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

        // Action Buttons at bottom: EXAMINE, SEND TO LAB, LINK TO SUSPECT
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionRow.setOpaque(false);

        CustomButton btnExamine = new CustomButton("EXAMINE", CustomButton.ButtonStyle.DARK);
        btnExamine.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Microscopic Examination: Traces of digitalis poison confirmed along with faint thumbprints on the stem.",
                "Forensic Examination", JOptionPane.INFORMATION_MESSAGE));

        CustomButton btnLab = new CustomButton("SEND TO LAB", CustomButton.ButtonStyle.DARK);
        btnLab.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Sample sent to Toxicology Department. Results match Case #103 toxin profile.",
                "Forensic Lab", JOptionPane.INFORMATION_MESSAGE));

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
        EvidenceItem item = evidenceList.get(index);

        lblId.setText("Evidence ID: " + item.id);
        lblType.setText("Evidence Type: " + item.type);
        lblLocation.setText("Location: " + item.location);
        lblTime.setText("Time Collected: " + item.time);
        lblDescription.setText("<html><b>Description:</b> " + item.description + "</html>");

        for (int i = 0; i < listItemPanels.size(); i++) {
            GlassPanel p = listItemPanels.get(i);
            p.setSelected(i == index);
            JLabel lbl = (JLabel) p.getComponent(0);
            lbl.setForeground(i == index ? Theme.TEXT_GOLD : Theme.TEXT_PRIMARY);
        }

        photoPanel.repaint();
    }
}
