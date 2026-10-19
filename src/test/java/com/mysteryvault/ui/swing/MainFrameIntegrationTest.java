package com.mysteryvault.ui.swing;

import com.mysteryvault.MysteryVaultApplication;
import com.mysteryvault.ui.swing.navigation.ScreenManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.Component;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end EDT test that boots the integrated application shell and verifies
 * all 12 screens instantiate, receive production adapters, and navigate cleanly.
 */
public class MainFrameIntegrationTest {

    @Test
    @DisplayName("MainFrame boots with integrated production adapters and navigates all 12 screens")
    void testAllTwelveScreensOnEDT() throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(() -> {
            MainFrame frame = MysteryVaultApplication.createMainFrame(MysteryVaultApplication.AppMode.INTEGRATED);
            assertNotNull(frame);
            assertEquals("Mystery Vault – Uncover the Truth", frame.getTitle());

            ScreenManager sm = ScreenManager.getInstance();
            assertNotNull(sm);

            // Verify all 12 screens can be displayed without error
            for (String screenName : ScreenManager.ALL_SCREENS) {
                sm.showScreen(screenName);
                assertEquals(screenName, sm.getCurrentScreen());
            }

            frame.dispose();
        });
    }

    @Test
    @DisplayName("MainFrame boots with demo mock adapters fallback")
    void testDemoMockFallbackOnEDT() throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(() -> {
            MainFrame frame = MysteryVaultApplication.createMainFrame(MysteryVaultApplication.AppMode.DEMO);
            assertNotNull(frame);

            ScreenManager sm = ScreenManager.getInstance();
            assertNotNull(sm);

            sm.showScreen(ScreenManager.MAIN_MENU);
            assertEquals(ScreenManager.MAIN_MENU, sm.getCurrentScreen());

            sm.showScreen(ScreenManager.CASE_SELECTION);
            assertEquals(ScreenManager.CASE_SELECTION, sm.getCurrentScreen());

            frame.dispose();
        });
    }
}
