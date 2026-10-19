package com.mysteryvault.screens;

import com.mysteryvault.components.*;
import com.mysteryvault.model.Case;
import com.mysteryvault.navigation.ScreenManager;
import com.mysteryvault.service.CaseService;
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
 * Screen 3: Case Selection Gallery (Module 1 UI integration).
 * Database-driven case list loading real case information from repository.
 */
public class CaseSelectionScreen extends JPanel {

    private int selectedCaseIndex = 0;
    private final List<GlassPanel> caseCards = new ArrayList<>();
    private final List<Case> loadedCases = new ArrayList<>();
    private JPanel gridPanel;
    private JPanel centerContainer;

    public CaseSelectionScreen() {
        setOpaque(true);
        setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());

        initUI();
    }

    private void initUI() {
        ScreenHeader header = new ScreenHeader("3. CASE SELECTION");
        add(header, BorderLayout.NORTH);

        centerContainer = new JPanel();
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));
        centerContainer.setOpaque(false);
        centerContainer.setBorder(BorderFactory.createEmptyBorder(10, 80, 20, 80));

        gridPanel = new JPanel(new GridLayout(2, 2, 24, 20));
        gridPanel.setOpaque(false);
        gridPanel.setMaximumSize(new Dimension(860, 480));

        loadCasesFromRepository();

        // Bottom Button Container
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 16));
        bottomPanel.setOpaque(false);

        CustomButton btnStart = new CustomButton("START INVESTIGATION", CustomButton.ButtonStyle.GOLD);
        btnStart.setPreferredSize(new Dimension(240, 42));
        btnStart.addActionListener(e -> {
            if (!loadedCases.isEmpty() && selectedCaseIndex >= 0 && selectedCaseIndex < loadedCases.size()) {
                ScreenManager.getInstance().setSelectedCaseId(loadedCases.get(selectedCaseIndex).getId());
            }
            ScreenManager.getInstance().showScreen(ScreenManager.INVESTIGATION_HUB);
        });

        CustomButton btnRefresh = new CustomButton("REFRESH CASES", CustomButton.ButtonStyle.DARK);
        btnRefresh.setPreferredSize(new Dimension(160, 42));
        btnRefresh.addActionListener(e -> reload());

        bottomPanel.add(btnStart);
        bottomPanel.add(btnRefresh);

        centerContainer.add(gridPanel);
        centerContainer.add(bottomPanel);

        add(centerContainer, BorderLayout.CENTER);
    }

    public void reload() {
        gridPanel.removeAll();
        caseCards.clear();
        loadedCases.clear();
        loadCasesFromRepository();
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private void loadCasesFromRepository() {
        try {
            CaseService service = ScreenManager.getInstance().getCaseService();
            List<Case> cases = service.listCases();

            if (cases.isEmpty()) {
                // Empty state
                gridPanel.setLayout(new BorderLayout());
                JPanel emptyPanel = new JPanel(new GridBagLayout());
                emptyPanel.setOpaque(false);

                JPanel msgBox = new JPanel();
                msgBox.setLayout(new BoxLayout(msgBox, BoxLayout.Y_AXIS));
                msgBox.setOpaque(false);

                JLabel lblEmpty = new JLabel("No investigation cases found in repository.");
                lblEmpty.setFont(Theme.bold(16));
                lblEmpty.setForeground(Theme.TEXT_MUTED);
                lblEmpty.setAlignmentX(Component.CENTER_ALIGNMENT);

                CustomButton btnLoadSample = new CustomButton("LOAD SAMPLE CASE #103", CustomButton.ButtonStyle.GOLD);
                btnLoadSample.setAlignmentX(Component.CENTER_ALIGNMENT);
                btnLoadSample.addActionListener(e -> {
                    service.loadSampleCase103();
                    reload();
                });

                msgBox.add(lblEmpty);
                msgBox.add(Box.createVerticalStrut(16));
                msgBox.add(btnLoadSample);

                emptyPanel.add(msgBox);
                gridPanel.add(emptyPanel, BorderLayout.CENTER);
                return;
            }

            // Database populated state
            gridPanel.setLayout(new GridLayout(Math.max(2, (int) Math.ceil(cases.size() / 2.0)), 2, 24, 20));
            loadedCases.addAll(cases);

            for (int i = 0; i < cases.size(); i++) {
                Case c = cases.get(i);
                boolean isSelected = (i == 0);
                if (isSelected) {
                    ScreenManager.getInstance().setSelectedCaseId(c.getId());
                }

                String badgeText = c.getStatus() != null ? c.getStatus().name() : "OPEN";
                Badge.BadgeType badgeType = mapStatusToBadge(c.getStatus());
                String subtitle = String.format("Priority: %s • Suspects: %d • Evidence: %d",
                        c.getPriority() != null ? c.getPriority().getDisplayName() : "Medium",
                        c.getSuspectIds().size(),
                        c.getEvidenceIds().size());

                String title = String.format("%s – %s", c.getId(), c.getTitle());
                String imgKey = (i % 2 == 0) ? "case_poison.png" : "case_diamond.png";

                gridPanel.add(createCaseCard(i, c, imgKey, title, subtitle, badgeText, badgeType, isSelected));
            }

        } catch (Exception e) {
            // Error State
            gridPanel.setLayout(new BorderLayout());
            JLabel errLbl = new JLabel("Database Error: Failed to query cases from repository: " + e.getMessage());
            errLbl.setForeground(new Color(220, 80, 80));
            errLbl.setFont(Theme.regular(13));
            errLbl.setHorizontalAlignment(SwingConstants.CENTER);
            gridPanel.add(errLbl, BorderLayout.CENTER);
        }
    }

    private Badge.BadgeType mapStatusToBadge(com.mysteryvault.enums.CaseStatus status) {
        if (status == null) return Badge.BadgeType.IN_PROGRESS;
        return switch (status) {
            case SOLVED -> Badge.BadgeType.SOLVED;
            case ARCHIVED -> Badge.BadgeType.LOCKED;
            default -> Badge.BadgeType.IN_PROGRESS;
        };
    }

    private JPanel createCaseCard(int index, Case c, String imgKey, String title, String subtitle,
                                  String badgeText, Badge.BadgeType badgeType, boolean isSelectedDefault) {
        GlassPanel card = new GlassPanel(new BorderLayout(0, 8));
        card.setCornerRadius(12);
        card.setSelected(isSelectedDefault);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        caseCards.add(card);

        // Top Thumbnail with badge in top right
        JPanel thumbPanel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(360, 130);
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.applyQualityHints(g2);

                int w = getWidth();
                int h = getHeight();

                BufferedImage img = AssetManager.getImage(imgKey);
                BufferedImage rounded = AssetManager.getRoundedImage(img, w, h, 8, Theme.BORDER_SUBTLE);
                g2.drawImage(rounded, 0, 0, null);

                g2.dispose();
            }
        };
        thumbPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        thumbPanel.setOpaque(false);

        Badge badge = new Badge(badgeText, badgeType);
        thumbPanel.add(badge);

        // Bottom Info Panel
        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 3));
        infoPanel.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(Theme.bold(14));
        titleLbl.setForeground(isSelectedDefault ? Theme.TEXT_GOLD : Theme.TEXT_PRIMARY);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(Theme.regular(12));
        subLbl.setForeground(Theme.TEXT_MUTED);

        infoPanel.add(titleLbl);
        infoPanel.add(subLbl);

        card.add(thumbPanel, BorderLayout.CENTER);
        card.add(infoPanel, BorderLayout.SOUTH);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedCaseIndex = index;
                ScreenManager.getInstance().setSelectedCaseId(c.getId());
                for (int i = 0; i < caseCards.size(); i++) {
                    caseCards.get(i).setSelected(i == index);
                }
            }
        });

        return card;
    }
}
