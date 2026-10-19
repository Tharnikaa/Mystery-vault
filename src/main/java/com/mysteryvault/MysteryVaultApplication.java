package com.mysteryvault;

import com.mysteryvault.navigation.ScreenManager;
import com.mysteryvault.screens.*;
import com.mysteryvault.theme.AssetManager;
import com.mysteryvault.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class MysteryVaultApplication {

    public static void main(String[] args) {
        // Set system look and feel hints
        System.setProperty("sun.java2d.uiScale.enabled", "true");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        initializeBackend();

        SwingUtilities.invokeLater(MysteryVaultApplication::createAndShowGUI);
    }

    private static void initializeBackend() {
        try {
            com.mysteryvault.config.DatabaseConfig dbConfig = com.mysteryvault.config.DatabaseConfig.fromEnvironment();
            if (dbConfig.isAvailable()) {
                System.out.println("Connecting Mystery Vault to PostgreSQL database at: " + dbConfig.getMaskedUrl());
                com.mysteryvault.config.DatabaseConnectionFactory factory = new com.mysteryvault.config.DatabaseConnectionFactory(dbConfig);
                factory.initializeSchema();
                com.mysteryvault.service.CaseService caseService = com.mysteryvault.service.CaseService.createDatabaseService(factory);
                com.mysteryvault.service.SuspectService suspectService = new com.mysteryvault.service.SuspectService(
                        new com.mysteryvault.repository.impl.SuspectJdbcRepository(factory));
                com.mysteryvault.service.EvidenceService evidenceService = new com.mysteryvault.service.EvidenceService(
                        new com.mysteryvault.repository.impl.EvidenceJdbcRepository(factory),
                        new com.mysteryvault.repository.impl.ClueJdbcRepository(factory));

                // If empty database, seed Case #103
                if (caseService.listCases().isEmpty()) {
                    com.mysteryvault.service.SampleDataFactory.populateRepositories(
                            new com.mysteryvault.repository.impl.CaseJdbcRepository(factory),
                            new com.mysteryvault.repository.impl.SuspectJdbcRepository(factory),
                            new com.mysteryvault.repository.impl.EvidenceJdbcRepository(factory),
                            new com.mysteryvault.repository.impl.ClueJdbcRepository(factory));
                }

                ScreenManager sm = ScreenManager.getInstance();
                if (sm != null) {
                    sm.setCaseService(caseService);
                    sm.setSuspectService(suspectService);
                    sm.setEvidenceService(evidenceService);
                }
            } else {
                System.out.println("Notice: PostgreSQL not available at " + dbConfig.getMaskedUrl() + ". Using in-memory persistence for demo.");
                com.mysteryvault.service.CaseService memService = com.mysteryvault.service.CaseService.createInMemoryService();
                memService.loadSampleCase103();
                ScreenManager sm = ScreenManager.getInstance();
                if (sm != null) {
                    sm.setCaseService(memService);
                }
            }
        } catch (Throwable t) {
            System.err.println("Backend initialization note: " + t.getMessage());
        }
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Mystery Vault – Uncover the Truth");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(1440, 900));
        frame.setMinimumSize(new Dimension(1024, 700));

        // Window Icon
        BufferedImage appIcon = AssetManager.getImage("detective_portrait.jpg");
        if (appIcon != null) {
            frame.setIconImage(appIcon);
        }

        // Main layout container
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(Theme.BG_DARK);

        // CardLayout container for all 12 screens
        CardLayout cardLayout = new CardLayout();
        JPanel screenContainer = new JPanel(cardLayout);
        screenContainer.setOpaque(false);

        ScreenManager screenManager = new ScreenManager(screenContainer, cardLayout);

        // 1. Main Menu
        screenContainer.add(new MainMenuScreen(), ScreenManager.MAIN_MENU);
        // 2. Detective Profile
        screenContainer.add(new DetectiveProfileScreen(), ScreenManager.DETECTIVE_PROFILE);
        // 3. Case Selection
        screenContainer.add(new CaseSelectionScreen(), ScreenManager.CASE_SELECTION);
        // 4. Main Investigation Hub
        screenContainer.add(new InvestigationHubScreen(), ScreenManager.INVESTIGATION_HUB);
        // 5. Suspect Screen
        screenContainer.add(new SuspectScreen(), ScreenManager.SUSPECT_SCREEN);
        // 6. Evidence Investigation
        screenContainer.add(new EvidenceScreen(), ScreenManager.EVIDENCE_INVESTIGATION);
        // 7. Interrogation
        screenContainer.add(new InterrogationScreen(), ScreenManager.INTERROGATION);
        // 8. Investigation Timeline
        screenContainer.add(new TimelineScreen(), ScreenManager.INVESTIGATION_TIMELINE);
        // 9. Case Connection Engine
        screenContainer.add(new ConnectionEngineScreen(), ScreenManager.CASE_CONNECTION_ENGINE);
        // 10. Case Analysis
        screenContainer.add(new CaseAnalysisScreen(), ScreenManager.CASE_ANALYSIS);
        // 11. Final Deduction
        screenContainer.add(new FinalDeductionScreen(), ScreenManager.FINAL_DEDUCTION);
        // 12. Case Result
        screenContainer.add(new CaseResultScreen(), ScreenManager.CASE_RESULT);

        rootPanel.add(screenContainer, BorderLayout.CENTER);

        // Sleek Top Quick-Nav Bar for rapid evaluation of all 12 screens
        JPanel quickBar = createQuickNavBar(screenManager);
        rootPanel.add(quickBar, BorderLayout.SOUTH);

        frame.setContentPane(rootPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);

        // Keyboard hotkeys (1-9, 0, -, =) for quick screen switching
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                if (code >= KeyEvent.VK_1 && code <= KeyEvent.VK_9) {
                    int idx = code - KeyEvent.VK_1;
                    if (idx < ScreenManager.ALL_SCREENS.length) {
                        screenManager.showScreen(ScreenManager.ALL_SCREENS[idx]);
                    }
                } else if (code == KeyEvent.VK_0) {
                    screenManager.showScreen(ScreenManager.ALL_SCREENS[9]); // 10. Case Analysis
                } else if (code == KeyEvent.VK_MINUS) {
                    screenManager.showScreen(ScreenManager.ALL_SCREENS[10]); // 11. Final Deduction
                } else if (code == KeyEvent.VK_EQUALS) {
                    screenManager.showScreen(ScreenManager.ALL_SCREENS[11]); // 12. Case Result
                }
            }
        });

        frame.setVisible(true);
    }

    private static JPanel createQuickNavBar(ScreenManager screenManager) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(10, 13, 20));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_SUBTLE));
        bar.setPreferredSize(new Dimension(1440, 42));

        // Left info label
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 8));
        left.setOpaque(false);
        JLabel lblPrompt = new JLabel("MYSTERY VAULT NAVIGATION:  Use buttons or Screen Switcher to evaluate all 12 screens");
        lblPrompt.setFont(Theme.regular(11));
        lblPrompt.setForeground(Theme.TEXT_MUTED);
        left.add(lblPrompt);

        // Right JComboBox selector
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 4));
        right.setOpaque(false);

        JLabel lblSelect = new JLabel("Jump To Screen:");
        lblSelect.setFont(Theme.bold(11));
        lblSelect.setForeground(Theme.TEXT_GOLD);

        String[] comboItems = new String[ScreenManager.ALL_SCREENS.length];
        for (int i = 0; i < ScreenManager.ALL_SCREENS.length; i++) {
            comboItems[i] = (i + 1) + ". " + ScreenManager.ALL_SCREENS[i];
        }

        JComboBox<String> screenCombo = new JComboBox<>(comboItems);
        screenCombo.setFont(Theme.regular(12));
        screenCombo.setBackground(new Color(22, 29, 44));
        screenCombo.setForeground(Theme.TEXT_PRIMARY);
        screenCombo.setFocusable(false);
        screenCombo.setPreferredSize(new Dimension(240, 28));

        screenCombo.addActionListener(e -> {
            int selected = screenCombo.getSelectedIndex();
            if (selected >= 0 && selected < ScreenManager.ALL_SCREENS.length) {
                screenManager.showScreen(ScreenManager.ALL_SCREENS[selected]);
            }
        });

        right.add(lblSelect);
        right.add(screenCombo);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);

        return bar;
    }
}
