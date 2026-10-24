package com.mysteryvault.ui.swing.navigation;

/**
 * Interface implemented by UI screen panels that dynamically refresh
 * their content based on the active case and updated database state.
 */
public interface RefreshableScreen {
    void refreshScreen(String caseId);
}
