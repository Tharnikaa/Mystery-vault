package com.mysteryvault.ui.swing.screens;

import com.mysteryvault.ui.swing.components.*;
import com.mysteryvault.ui.swing.mock.MockCaseViewPort;
import com.mysteryvault.ui.swing.navigation.ScreenManager;
import com.mysteryvault.ui.swing.ports.CaseViewPort;
import com.mysteryvault.ui.swing.theme.AssetManager;
import com.mysteryvault.ui.swing.theme.Theme;
import com.mysteryvault.ui.swing.viewmodel.CaseCardViewModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class CaseSelectionPanel extends JPanel {

    private final CaseViewPort casePort;
    private int selectedCaseIndex = 2; // Case #103 selected by default
    private final List<GlassPanel> caseCards = new ArrayList<>();

    public CaseSelectionPanel(CaseViewPort casePort) {
        this.casePort = casePort != null ? casePort : new MockCaseViewPort();
        setOpaque(true);
        setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());

        initUI();
    }

    public CaseSelectionPanel() {
        this(new MockCaseViewPort());
    }

    private void initUI() {
        ScreenHeader header = new ScreenHeader("3. CASE SELECTION");
        add(header, BorderLayout.NORTH);

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setOpaque(false);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(10, 80, 20, 80));

        JPanel gridPanel = new JPanel(new GridLayout(2, 2, 24, 20));
        gridPanel.setOpaque(false);
        gridPanel.setMaximumSize(new Dimension(860, 480));

        List<CaseCardViewModel> availableCases = casePort.getAvailableCases();
        for (int i = 0; i < availableCases.size(); i++) {
            CaseCardViewModel vm = availableCases.get(i);
            Badge.BadgeType bType = Badge.BadgeType.LOCKED;
            if ("SOLVED".equalsIgnoreCase(vm.getBadgeType())) bType = Badge.BadgeType.SOLVED;
            else if ("IN_PROGRESS".equalsIgnoreCase(vm.getBadgeType())) bType = Badge.BadgeType.IN_PROGRESS;

            gridPanel.add(createCaseCard(i, vm.getImageKey(), vm.getTitle(), vm.getDuration(),
                    vm.getStatusBadge(), bType, vm.isSelectedDefault()));
        }

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 16));
        bottomPanel.setOpaque(false);

        CustomButton btnStart = new CustomButton("START INVESTIGATION", CustomButton.ButtonStyle.GOLD);
        btnStart.setPreferredSize(new Dimension(240, 42));
        btnStart.addActionListener(e -> {
            if (selectedCaseIndex >= 0 && selectedCaseIndex < availableCases.size()) {
                casePort.selectCase(availableCases.get(selectedCaseIndex).getId());
            }
            ScreenManager.getInstance().showScreen(ScreenManager.INVESTIGATION_HUB);
        });

        bottomPanel.add(btnStart);

        mainContainer.add(gridPanel);
        mainContainer.add(bottomPanel);

        add(mainContainer, BorderLayout.CENTER);
    }

    private JPanel createCaseCard(int index, String imgKey, String title, String subtitle,
                                  String badgeText, Badge.BadgeType badgeType, boolean isSelectedDefault) {
        GlassPanel card = new GlassPanel(new BorderLayout(0, 8));
        card.setCornerRadius(12);
        card.setSelected(isSelectedDefault);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        caseCards.add(card);

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
                for (int i = 0; i < caseCards.size(); i++) {
                    caseCards.get(i).setSelected(i == index);
                }
                List<CaseCardViewModel> cases = casePort.getAvailableCases();
                if (index >= 0 && index < cases.size()) {
                    casePort.selectCase(cases.get(index).getId());
                }
            }
        });

        return card;
    }
}
