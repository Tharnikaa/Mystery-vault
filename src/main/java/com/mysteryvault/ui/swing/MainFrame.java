package com.mysteryvault.ui.swing;

import com.mysteryvault.service.CaseSolverEngine;
import com.mysteryvault.ui.swing.dialogs.NewCaseDialog;
import com.mysteryvault.ui.swing.mock.*;
import com.mysteryvault.ui.swing.navigation.ScreenManager;
import com.mysteryvault.ui.swing.ports.*;
import com.mysteryvault.ui.swing.screens.*;
import com.mysteryvault.ui.swing.theme.AssetManager;
import com.mysteryvault.ui.swing.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

/**
 * Module 13 — Main Application Window Shell.
 *
 * Owned by Member 4.
 * Hosts the CardLayout screen container, wires frontend presentation ports,
 * and manages global keyboard shortcuts and the evaluation screen switcher.
 */
public class MainFrame extends JFrame {

    private final ScreenManager screenManager;
    private final CardLayout cardLayout;
    private final JPanel screenContainer;

    // Presentation ports (injected at composition root)
    private final CaseViewPort casePort;
    private final SuspectViewPort suspectPort;
    private final EvidenceViewPort evidencePort;
    private final TimelineViewPort timelinePort;
    private final InterrogationViewPort interrogationPort;
    private final ConnectionViewPort connectionPort;
    private final AnalysisViewPort analysisPort;
    private final DeductionViewPort deductionPort;
    private final ReportViewPort reportPort;
    private final ProfileViewPort profilePort;

    public MainFrame() {
        this(
                new MockCaseViewPort(),
                new MockSuspectViewPort(),
                new MockEvidenceViewPort(),
                new MockTimelineViewPort(),
                new MockInterrogationViewPort(),
                new MockConnectionViewPort(),
                new MockAnalysisViewPort(),
                new MockDeductionViewPort(new CaseSolverEngine()),
                new MockReportViewPort(),
                new MockProfileViewPort()
        );
    }

    public MainFrame(CaseViewPort casePort,
                     SuspectViewPort suspectPort,
                     EvidenceViewPort evidencePort,
                     TimelineViewPort timelinePort,
                     InterrogationViewPort interrogationPort,
                     ConnectionViewPort connectionPort,
                     AnalysisViewPort analysisPort,
                     DeductionViewPort deductionPort,
                     ReportViewPort reportPort,
                     ProfileViewPort profilePort) {
        super("Mystery Vault – Uncover the Truth");

        this.casePort = casePort;
        this.suspectPort = suspectPort;
        this.evidencePort = evidencePort;
        this.timelinePort = timelinePort;
        this.interrogationPort = interrogationPort;
        this.connectionPort = connectionPort;
        this.analysisPort = analysisPort;
        this.deductionPort = deductionPort;
        this.reportPort = reportPort;
        this.profilePort = profilePort;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1440, 900));
        setMinimumSize(new Dimension(1024, 700));

        BufferedImage appIcon = AssetManager.getImage("detective_portrait.jpg");
        if (appIcon != null) {
            setIconImage(appIcon);
        }

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(Theme.BG_DARK);

        cardLayout = new CardLayout();
        screenContainer = new JPanel(cardLayout);
        screenContainer.setOpaque(false);

        screenManager = new ScreenManager(screenContainer, cardLayout);

        // Register all 12 screen panels
        screenContainer.add(new MainMenuPanel(), ScreenManager.MAIN_MENU);
        screenContainer.add(new DetectiveProfilePanel(this.profilePort), ScreenManager.DETECTIVE_PROFILE);
        screenContainer.add(new CaseSelectionPanel(this.casePort), ScreenManager.CASE_SELECTION);
        screenContainer.add(new InvestigationHubPanel(), ScreenManager.INVESTIGATION_HUB);
        screenContainer.add(new SuspectPanel(this.suspectPort), ScreenManager.SUSPECT_SCREEN);
        screenContainer.add(new EvidencePanel(this.evidencePort), ScreenManager.EVIDENCE_INVESTIGATION);
        screenContainer.add(new InterrogationPanel(this.interrogationPort, this.suspectPort), ScreenManager.INTERROGATION);
        screenContainer.add(new TimelinePanel(this.timelinePort), ScreenManager.INVESTIGATION_TIMELINE);
        screenContainer.add(new ConnectionPanel(this.connectionPort), ScreenManager.CASE_CONNECTION_ENGINE);
        screenContainer.add(new AnalysisPanel(this.analysisPort), ScreenManager.CASE_ANALYSIS);
        screenContainer.add(new DeductionPanel(this.deductionPort, this.casePort), ScreenManager.FINAL_DEDUCTION);
        screenContainer.add(new ResultPanel(this.deductionPort, this.reportPort), ScreenManager.CASE_RESULT);

        rootPanel.add(screenContainer, BorderLayout.CENTER);

        // Sleek bottom evaluation bar
        JPanel quickBar = createQuickNavBar();
        rootPanel.add(quickBar, BorderLayout.SOUTH);

        setContentPane(rootPanel);
        pack();
        setLocationRelativeTo(null);

        initKeyboardShortcuts();
    }

    private void initKeyboardShortcuts() {
        addKeyListener(new KeyAdapter() {
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
                } else if (code == KeyEvent.VK_N && (e.isControlDown() || e.isMetaDown())) {
                    openNewCaseDialog();
                }
            }
        });
    }

    public void openNewCaseDialog() {
        NewCaseDialog dlg = new NewCaseDialog(this);
        dlg.setOnCaseCreated(newCase -> {
            JOptionPane.showMessageDialog(this,
                    "New case created: " + newCase.getTitle(),
                    "Case Drafted", JOptionPane.INFORMATION_MESSAGE);
        });
        dlg.setVisible(true);
    }

    private JPanel createQuickNavBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(10, 13, 20));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_SUBTLE));
        bar.setPreferredSize(new Dimension(1440, 42));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 8));
        left.setOpaque(false);
        JLabel lblPrompt = new JLabel("MYSTERY VAULT NAVIGATION:  Use in-game buttons, hotkeys (1-9, 0, -, =), or dropdown to evaluate all 12 screens");
        lblPrompt.setFont(Theme.regular(11));
        lblPrompt.setForeground(Theme.TEXT_MUTED);
        left.add(lblPrompt);

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

        screenManager.setScreenSelector(screenCombo);

        right.add(lblSelect);
        right.add(screenCombo);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);

        return bar;
    }

    public ScreenManager getScreenManager() {
        return screenManager;
    }
}
