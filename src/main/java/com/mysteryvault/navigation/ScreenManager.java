package com.mysteryvault.navigation;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayDeque;
import java.util.Deque;

public class ScreenManager {

    public static final String MAIN_MENU = "Main Menu";
    public static final String DETECTIVE_PROFILE = "Detective Profile";
    public static final String CASE_SELECTION = "Case Selection";
    public static final String INVESTIGATION_HUB = "Main Investigation Hub";
    public static final String SUSPECT_SCREEN = "Suspect Screen";
    public static final String EVIDENCE_INVESTIGATION = "Evidence Investigation";
    public static final String INTERROGATION = "Interrogation";
    public static final String INVESTIGATION_TIMELINE = "Investigation Timeline";
    public static final String CASE_CONNECTION_ENGINE = "Case Connection Engine";
    public static final String CASE_ANALYSIS = "Case Analysis";
    public static final String FINAL_DEDUCTION = "Final Deduction";
    public static final String CASE_RESULT = "Case Result";

    public static final String[] ALL_SCREENS = {
            MAIN_MENU,
            DETECTIVE_PROFILE,
            CASE_SELECTION,
            INVESTIGATION_HUB,
            SUSPECT_SCREEN,
            EVIDENCE_INVESTIGATION,
            INTERROGATION,
            INVESTIGATION_TIMELINE,
            CASE_CONNECTION_ENGINE,
            CASE_ANALYSIS,
            FINAL_DEDUCTION,
            CASE_RESULT
    };

    private static ScreenManager instance;

    private final JPanel container;
    private final CardLayout cardLayout;
    private final Deque<String> navigationHistory = new ArrayDeque<>();
    private String currentScreen = MAIN_MENU;
    private JComboBox<String> screenSelector;
    private String selectedCaseId = "CASE-103";

    private com.mysteryvault.service.CaseService caseService;
    private com.mysteryvault.service.SuspectService suspectService;
    private com.mysteryvault.service.EvidenceService evidenceService;

    public ScreenManager(JPanel container, CardLayout cardLayout) {
        this.container = container;
        this.cardLayout = cardLayout;
        instance = this;
    }

    public static ScreenManager getInstance() {
        return instance;
    }

    public String getSelectedCaseId() {
        return selectedCaseId;
    }

    public void setSelectedCaseId(String selectedCaseId) {
        if (selectedCaseId != null && !selectedCaseId.trim().isEmpty()) {
            this.selectedCaseId = selectedCaseId.trim();
        }
    }

    public com.mysteryvault.service.CaseService getCaseService() {
        if (caseService == null) {
            caseService = com.mysteryvault.service.CaseService.createInMemoryService();
        }
        return caseService;
    }

    public void setCaseService(com.mysteryvault.service.CaseService caseService) {
        this.caseService = caseService;
    }

    public com.mysteryvault.service.SuspectService getSuspectService() {
        if (suspectService == null) {
            suspectService = new com.mysteryvault.service.SuspectService();
        }
        return suspectService;
    }

    public void setSuspectService(com.mysteryvault.service.SuspectService suspectService) {
        this.suspectService = suspectService;
    }

    public com.mysteryvault.service.EvidenceService getEvidenceService() {
        if (evidenceService == null) {
            evidenceService = new com.mysteryvault.service.EvidenceService();
        }
        return evidenceService;
    }

    public void setEvidenceService(com.mysteryvault.service.EvidenceService evidenceService) {
        this.evidenceService = evidenceService;
    }

    public void setScreenSelector(JComboBox<String> selector) {
        this.screenSelector = selector;
    }

    public void showScreen(String screenName) {
        if (!screenName.equals(currentScreen)) {
            navigationHistory.push(currentScreen);
        }
        currentScreen = screenName;
        cardLayout.show(container, screenName);

        if (screenSelector != null && !screenName.equals(screenSelector.getSelectedItem())) {
            screenSelector.setSelectedItem(screenName);
        }
    }

    public void goBack() {
        if (!navigationHistory.isEmpty()) {
            String prev = navigationHistory.pop();
            currentScreen = prev;
            cardLayout.show(container, prev);
            if (screenSelector != null) {
                screenSelector.setSelectedItem(prev);
            }
        } else {
            // Default back to Hub or Main Menu
            if (!currentScreen.equals(MAIN_MENU)) {
                showScreen(INVESTIGATION_HUB);
            }
        }
    }

    public String getCurrentScreen() {
        return currentScreen;
    }
}
