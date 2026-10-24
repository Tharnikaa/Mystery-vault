package com.mysteryvault.ui;

import com.mysteryvault.database.CaseDatabaseManager;
import com.mysteryvault.ui.swing.MainFrame;
import com.mysteryvault.ui.swing.adapters.*;
import com.mysteryvault.ui.swing.components.CustomButton;
import com.mysteryvault.ui.swing.components.GlassPanel;
import com.mysteryvault.ui.swing.navigation.ScreenManager;
import com.mysteryvault.ui.swing.ports.EvidenceViewPort;
import com.mysteryvault.ui.swing.ports.SuspectViewPort;
import com.mysteryvault.ui.swing.screens.InterrogationPanel;
import com.mysteryvault.ui.swing.screens.InvestigationHubPanel;
import com.mysteryvault.ui.swing.screens.SuspectPanel;
import com.mysteryvault.ui.swing.theme.Theme;
import com.mysteryvault.ui.swing.viewmodel.EvidenceViewModel;
import com.mysteryvault.ui.swing.viewmodel.SuspectViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SuspectAndHubUiTest {

    @TempDir
    Path tempDir;

    private CaseDatabaseManager dbManager;
    private SuspectViewPort suspectPort;
    private EvidenceViewPort evidencePort;
    private IntegratedInterrogationAdapter interrogationPort;
    private SuspectPanel suspectPanel;
    private InvestigationHubPanel hubPanel;
    private InterrogationPanel interrogationPanel;

    @BeforeEach
    void setUp() throws SQLException {
        dbManager = new CaseDatabaseManager(tempDir.toFile());
        dbManager.initializeAndSeedAll();

        suspectPort = new IntegratedSuspectAdapter(dbManager);
        evidencePort = new IntegratedEvidenceAdapter(dbManager);
        interrogationPort = new IntegratedInterrogationAdapter(dbManager);

        suspectPanel = new SuspectPanel(suspectPort, evidencePort);
        hubPanel = new InvestigationHubPanel(new IntegratedCaseAdapter(dbManager));
        interrogationPanel = new InterrogationPanel(interrogationPort, suspectPort);

        // Setup ScreenManager with container for navigation tests
        JPanel container = new JPanel(new CardLayout());
        CardLayout cl = (CardLayout) container.getLayout();
        ScreenManager sm = new ScreenManager(container, cl);
        container.add(suspectPanel, ScreenManager.SUSPECT_SCREEN);
        container.add(interrogationPanel, ScreenManager.INTERROGATION);
        container.add(hubPanel, ScreenManager.INVESTIGATION_HUB);
    }

    @Test
    @DisplayName("1. Every suspect card has working VIEW DETAILS button operating on that suspect")
    void testEverySuspectCardHasViewDetailsButton() {
        List<SuspectViewModel> suspects = suspectPort.getSuspectsForCase("CASE_101");
        assertFalse(suspects.isEmpty());
        List<GlassPanel> cards = suspectPanel.getSuspectCards();
        assertEquals(suspects.size(), cards.size(), "Every suspect in case must have a card");

        for (int i = 0; i < suspects.size(); i++) {
            SuspectViewModel expectedSuspect = suspects.get(i);
            GlassPanel card = cards.get(i);

            // Locate buttons inside the card
            List<CustomButton> buttons = findButtons(card);
            assertTrue(buttons.size() >= 3, "Suspect card for " + expectedSuspect.getName() + " must have at least 3 buttons");

            CustomButton detailsBtn = buttons.stream()
                    .filter(b -> "VIEW DETAILS".equals(b.getText()))
                    .findFirst()
                    .orElse(null);
            assertNotNull(detailsBtn, "Card for " + expectedSuspect.getName() + " must contain VIEW DETAILS button");

            // Verify suspect data is genuine and contains full profile
            assertNotNull(expectedSuspect.getOccupation(), "Occupation must be available");
            assertNotNull(expectedSuspect.getAlibi(), "Alibi must be available");
            assertNotNull(expectedSuspect.getStatus(), "Status must be available");
            assertTrue(expectedSuspect.getAge() > 0, "Age must be available");
            assertFalse(expectedSuspect.getAlibi().isEmpty());
        }
    }

    @Test
    @DisplayName("2. Every suspect card has working VIEW EVIDENCE button linking to genuine evidence")
    void testEverySuspectCardHasViewEvidenceButton() {
        List<SuspectViewModel> suspects = suspectPort.getSuspectsForCase("CASE_101");
        List<GlassPanel> cards = suspectPanel.getSuspectCards();

        for (int i = 0; i < suspects.size(); i++) {
            SuspectViewModel suspect = suspects.get(i);
            GlassPanel card = cards.get(i);

            List<CustomButton> buttons = findButtons(card);
            CustomButton evidenceBtn = buttons.stream()
                    .filter(b -> "VIEW EVIDENCE".equals(b.getText()))
                    .findFirst()
                    .orElse(null);
            assertNotNull(evidenceBtn, "Card for " + suspect.getName() + " must contain VIEW EVIDENCE button");

            // Verify evidence query retrieves authentic linked records
            List<EvidenceViewModel> linkedEvidence = evidencePort.getEvidenceForSuspect("CASE_101", suspect.getId());
            if ("SUS_101_3".equals(suspect.getId())) { // Marcus Vance has 3 linked items
                assertFalse(linkedEvidence.isEmpty(), "Marcus Vance must have genuine linked evidence");
                assertTrue(linkedEvidence.stream().anyMatch(e -> "EVD_101_SEC_LOG".equals(e.getId())));
                assertTrue(linkedEvidence.stream().anyMatch(e -> "EVD_101_GLASS_CUTTER".equals(e.getId())));
                for (EvidenceViewModel ev : linkedEvidence) {
                    assertNotNull(ev.getId());
                    assertNotNull(ev.getTitle());
                    assertNotNull(ev.getSignificance());
                    assertNotNull(ev.getDescription());
                }
            }
        }
    }

    @Test
    @DisplayName("3. Every suspect card has working INTERROGATE button that opens interrogation for that suspect")
    void testEverySuspectCardHasInterrogateButton() {
        List<SuspectViewModel> suspects = suspectPort.getSuspectsForCase("CASE_101");
        List<GlassPanel> cards = suspectPanel.getSuspectCards();

        for (int i = 0; i < suspects.size(); i++) {
            SuspectViewModel suspect = suspects.get(i);
            GlassPanel card = cards.get(i);

            List<CustomButton> buttons = findButtons(card);
            CustomButton interrogateBtn = buttons.stream()
                    .filter(b -> "INTERROGATE".equals(b.getText()))
                    .findFirst()
                    .orElse(null);
            assertNotNull(interrogateBtn, "Card for " + suspect.getName() + " must contain INTERROGATE button");

            // Click the button and verify interrogation screen is updated with this suspect
            interrogateBtn.doClick();

            assertEquals(ScreenManager.INTERROGATION, ScreenManager.getInstance().getCurrentScreen());
            assertEquals(suspect.getId(), interrogationPanel.getCurrentSuspect().getId(),
                    "Interrogation screen must switch to suspect " + suspect.getName());
        }
    }

    @Test
    @DisplayName("4. All investigation hub tiles have visible gold borders in normal state")
    void testAllInvestigationHubTilesHaveGoldBorders() {
        List<InvestigationHubPanel.HubTile> tiles = hubPanel.getHubTiles();
        assertEquals(8, tiles.size(), "Investigation Hub must contain all 8 action tiles");

        String[] expectedTitles = {
                "SUSPECTS", "EVIDENCE", "TIMELINE", "INTERROGATE",
                "CONNECTIONS", "ANALYZE CASE", "FINAL DEDUCTION", "PROFILE"
        };

        for (int i = 0; i < expectedTitles.length; i++) {
            InvestigationHubPanel.HubTile tile = tiles.get(i);
            assertEquals(expectedTitles[i], tile.getTitle(), "Tile title must match " + expectedTitles[i]);

            // Requirement: "Every tile must have a visible gold border in its normal state"
            assertTrue(tile.hasGoldBorder(), "Tile " + tile.getTitle() + " must have a gold border");
            assertEquals(Theme.BORDER_GOLD, tile.getCurrentBorderColor(),
                    "Tile " + tile.getTitle() + " default border color must be Theme.BORDER_GOLD");
        }
    }

    private List<CustomButton> findButtons(Container container) {
        List<CustomButton> list = new ArrayList<>();
        for (Component c : container.getComponents()) {
            if (c instanceof CustomButton) {
                list.add((CustomButton) c);
            } else if (c instanceof Container) {
                list.addAll(findButtons((Container) c));
            }
        }
        return list;
    }
}
