package com.mysteryvault.progression;

import com.mysteryvault.database.CaseDatabaseManager;
import com.mysteryvault.model.DetectiveRank;
import com.mysteryvault.service.CaseCompletionService;
import com.mysteryvault.service.CaseSolverEngine;
import com.mysteryvault.service.dto.DeductionRequest;
import com.mysteryvault.service.dto.DeductionResult;
import com.mysteryvault.ui.swing.adapters.IntegratedDeductionAdapter;
import com.mysteryvault.ui.swing.adapters.IntegratedProfileAdapter;
import com.mysteryvault.ui.swing.viewmodel.ProfileViewModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

public class DetectiveProgressionTest {

    @TempDir
    Path tempDir;

    private CaseDatabaseManager dbManager;
    private CaseCompletionService completionService;
    private IntegratedDeductionAdapter deductionAdapter;
    private IntegratedProfileAdapter profileAdapter;

    @BeforeEach
    void setUp() throws SQLException {
        dbManager = new CaseDatabaseManager(tempDir.toFile());
        dbManager.initializeAndSeedAll();
        completionService = new CaseCompletionService(dbManager);
        deductionAdapter = new IntegratedDeductionAdapter(dbManager, new CaseSolverEngine(false));
        profileAdapter = new IntegratedProfileAdapter(dbManager);
    }

    @Test
    @DisplayName("5. A new detective starts with zero XP and zero completed cases")
    void testNewDetectiveStartsAtZero() {
        ProfileViewModel profile = profileAdapter.getPlayerProfile();
        assertNotNull(profile);
        assertEquals(0, profile.getXp(), "New detective must start with 0 XP");
        assertEquals(0, profile.getCasesSolved(), "New detective must have 0 completed cases");
        assertEquals(0, profile.getCasesAttempted(), "New detective must have 0 attempted cases");
        assertEquals("Rookie Detective", profile.getRank(), "Initial rank must be Rookie Detective");
        assertEquals("N/A", profile.getAccuracyDisplay(), "Accuracy must be displayed as N/A before attempts");
    }

    @Test
    @DisplayName("6. Rank changes at every threshold")
    void testRankChangesAtEveryThreshold() {
        assertEquals("Rookie Detective", DetectiveRank.getRankTitle(0));
        assertEquals("Rookie Detective", DetectiveRank.getRankTitle(99));

        assertEquals("Junior Detective", DetectiveRank.getRankTitle(100));
        assertEquals("Junior Detective", DetectiveRank.getRankTitle(299));

        assertEquals("Detective", DetectiveRank.getRankTitle(300));
        assertEquals("Detective", DetectiveRank.getRankTitle(599));

        assertEquals("Senior Detective", DetectiveRank.getRankTitle(600));
        assertEquals("Senior Detective", DetectiveRank.getRankTitle(999));

        assertEquals("Master Detective", DetectiveRank.getRankTitle(1000));
        assertEquals("Master Detective", DetectiveRank.getRankTitle(2500));
    }

    @Test
    @DisplayName("7. Correct deductions update the database immediately")
    void testCorrectDeductionUpdatesDatabaseImmediately() throws SQLException {
        DeductionRequest correctRequest = new DeductionRequest(
                "CASE_101",
                "SUS_101_3", // Marcus Vance
                "Financial desperation to pay off loan shark syndicates using a black-market diamond broker",
                new HashSet<>(Arrays.asList("EVD_101_SEC_LOG", "EVD_101_GLASS_CUTTER")),
                "Server RFID logs and biometric security records establish Marcus Vance accessed the vault during the blackout and cut the display with an engraved cutter."
        );

        DeductionResult result = deductionAdapter.submitDeduction(correctRequest);
        assertTrue(result.isSolved(), "Deduction should be verified as solved");
        assertEquals("CASE SOLVED", result.getVerdictTitle());

        // Check case metadata immediately in SQLite
        try (Connection conn = dbManager.getCaseConnection("CASE_101");
             PreparedStatement ps = conn.prepareStatement("SELECT status FROM case_metadata WHERE case_id = 'CASE_101'")) {
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals("SOLVED", rs.getString("status"), "Case status must immediately be SOLVED in case database");
            }
        }

        // Check catalog record immediately
        try (Connection conn = dbManager.getCatalogConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT is_completed FROM cases_catalog WHERE case_id = 'CASE_101'")) {
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt("is_completed"), "Catalog must mark case completed immediately");
            }
        }

        // Check player profile immediately
        ProfileViewModel profile = profileAdapter.getPlayerProfile();
        assertEquals(1, profile.getCasesSolved(), "Player profile must immediately reflect 1 solved case");
        int expectedXp = 100 + result.getScore();
        assertEquals(expectedXp, profile.getXp(), "Player XP must reflect 100 base + score reward");
        assertEquals("Junior Detective", profile.getRank(), "Rank must immediately update to Junior Detective (XP >= 100)");
    }

    @Test
    @DisplayName("8. Incorrect deductions leave the case unsolved")
    void testIncorrectDeductionsLeaveCaseUnsolved() throws SQLException {
        DeductionRequest incorrectRequest = new DeductionRequest(
                "CASE_101",
                "SUS_101_1", // Julian Drake (wrong suspect)
                "Wrong Motive",
                Collections.emptySet(),
                "Guessing without evidence"
        );

        DeductionResult result = deductionAdapter.submitDeduction(incorrectRequest);
        assertFalse(result.isSolved(), "Incorrect deduction must not solve the case");
        assertNotEquals("CASE SOLVED", result.getVerdictTitle());

        // Case status must still be OPEN
        try (Connection conn = dbManager.getCaseConnection("CASE_101");
             PreparedStatement ps = conn.prepareStatement("SELECT status FROM case_metadata WHERE case_id = 'CASE_101'")) {
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals("OPEN", rs.getString("status"), "Case status must remain OPEN");
            }
        }

        // XP must remain 0
        ProfileViewModel profile = profileAdapter.getPlayerProfile();
        assertEquals(0, profile.getXp(), "No XP should be awarded for incorrect deduction");
        assertEquals(0, profile.getCasesSolved(), "Cases solved must remain 0");
        assertEquals(1, profile.getCasesAttempted(), "Cases attempted must be recorded as 1");
        assertEquals("0%", profile.getAccuracyDisplay(), "Accuracy should now be 0% (0 / 1)");
    }

    @Test
    @DisplayName("9. Reopening the result screen does not award XP again")
    void testReopeningResultScreenDoesNotAwardXpAgain() throws SQLException {
        DeductionResult solvedResult = new DeductionResult(
                true, 90, "CASE SOLVED", "The Missing Diamond", "Explanation",
                "SOLVED", 5, 5, 3, 3, 2, 2, 0, "Junior Detective"
        );

        // First completion
        CaseCompletionService.CompletionOutcome outcome1 = completionService.completeCaseIfSolved("CASE_101", solvedResult);
        assertTrue(outcome1.isSuccess());
        assertFalse(outcome1.wasAlreadyCompleted());
        assertEquals(190, outcome1.getXpAwarded());
        assertEquals(190, profileAdapter.getPlayerProfile().getXp());

        // Reopening / submitting again
        CaseCompletionService.CompletionOutcome outcome2 = completionService.completeCaseIfSolved("CASE_101", solvedResult);
        assertTrue(outcome2.isSuccess(), "Idempotent completion should succeed safely");
        assertTrue(outcome2.wasAlreadyCompleted(), "Should be identified as already completed");
        assertEquals(0, outcome2.getXpAwarded(), "Should award 0 additional XP");

        ProfileViewModel profile = profileAdapter.getPlayerProfile();
        assertEquals(190, profile.getXp(), "XP must not be multiplied on screen reopen");
        assertEquals(1, profile.getCasesSolved(), "Cases solved count must remain 1");
    }

    @Test
    @DisplayName("10. Restarting the application preserving rank, XP, and solved cases")
    void testRestartPreservesRankXpAndSolvedCases() throws SQLException {
        // Solve case 101
        DeductionResult solvedResult = new DeductionResult(
                true, 85, "CASE SOLVED", "The Missing Diamond", "Explanation",
                "SOLVED", 5, 5, 3, 3, 2, 2, 0, "Junior Detective"
        );
        completionService.completeCaseIfSolved("CASE_101", solvedResult);

        // Simulate app shutdown and restart by instantiating new CaseDatabaseManager pointing to same directory
        CaseDatabaseManager restartedDbManager = new CaseDatabaseManager(tempDir.toFile());
        restartedDbManager.initializeAndSeedAll();

        IntegratedProfileAdapter restartedProfileAdapter = new IntegratedProfileAdapter(restartedDbManager);
        ProfileViewModel restartedProfile = restartedProfileAdapter.getPlayerProfile();

        assertEquals(185, restartedProfile.getXp(), "XP must survive restart");
        assertEquals(1, restartedProfile.getCasesSolved(), "Solved cases must survive restart");
        assertEquals("Junior Detective", restartedProfile.getRank(), "Rank must survive restart");

        // Verify case 101 is still marked completed in catalog
        try (Connection conn = restartedDbManager.getCatalogConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT is_completed FROM cases_catalog WHERE case_id = 'CASE_101'")) {
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt("is_completed"), "Case solved status must survive restart");
            }
        }
    }

    @Test
    @DisplayName("11. Repeated completion submissions not duplicating rewards")
    void testRepeatedSubmissionsDoNotDuplicateRewards() throws SQLException {
        DeductionResult solvedResult = new DeductionResult(
                true, 95, "CASE SOLVED", "The Missing Diamond", "Explanation",
                "SOLVED", 5, 5, 3, 3, 2, 2, 0, "JUNIOR DETECTIVE"
        );

        for (int i = 0; i < 5; i++) {
            completionService.completeCaseIfSolved("CASE_101", solvedResult);
        }

        ProfileViewModel profile = profileAdapter.getPlayerProfile();
        assertEquals(195, profile.getXp(), "XP must only be awarded once even after 5 repeated submissions");
        assertEquals(1, profile.getCasesSolved(), "Cases solved must remain exactly 1");
    }

    @Test
    @DisplayName("12. Database failures not producing false success messages")
    void testDatabaseFailuresDoNotProduceFalseSuccess() throws SQLException {
        // Create an uninitialized / invalid db manager to trigger failure
        File nonExistentDir = new File(tempDir.toFile(), "non_existent_subdir/invalid");
        CaseDatabaseManager faultyDbManager = new CaseDatabaseManager(nonExistentDir);
        CaseCompletionService faultyCompletionService = new CaseCompletionService(faultyDbManager);

        DeductionResult solvedResult = new DeductionResult(
                true, 90, "CASE SOLVED", "The Missing Diamond", "Explanation",
                "SOLVED", 5, 5, 3, 3, 2, 2, 0, "ROOKIE DETECTIVE"
        );

        CaseCompletionService.CompletionOutcome outcome = faultyCompletionService.completeCaseIfSolved("CASE_101", solvedResult);
        assertFalse(outcome.isSuccess(), "Outcome must report failure when DB operation fails");
        assertNotNull(outcome.getErrorMessage(), "Error message must be present");

        // IntegratedDeductionAdapter should NOT report CASE SOLVED if completion fails
        IntegratedDeductionAdapter adapterWithFaultyDb = new IntegratedDeductionAdapter(faultyDbManager, new CaseSolverEngine(false));
        DeductionRequest req = new DeductionRequest(
                "CASE_101", "SUS_101_3",
                "Financial desperation to pay off loan shark syndicates using a black-market diamond broker",
                new HashSet<>(Arrays.asList("EVD_101_SEC_LOG", "EVD_101_GLASS_CUTTER")),
                "Explanation"
        );

        DeductionResult res = adapterWithFaultyDb.submitDeduction(req);
        assertFalse(res.isSolved(), "Must not claim solved if DB persistence fails");
        assertNotEquals("CASE SOLVED", res.getVerdictTitle(), "Must not display CASE SOLVED on DB error");
    }
}
