package com.mysteryvault.service;

import com.mysteryvault.service.dto.CaseContext;
import com.mysteryvault.service.dto.DeductionRequest;
import com.mysteryvault.service.dto.DeductionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 test suite for CaseSolverEngine (Module 11).
 *
 * Verifies:
 * 1. Correct suspect, motive, and evidence evaluation
 * 2. Wrong suspect evaluation (CASE UNRESOLVED, wrong accusation counted)
 * 3. Wrong motive evaluation (strict matching, no broad substring match)
 * 4. Missing required evidence
 * 5. Empty evidence selection
 * 6. Unknown case ID (no fallback to CASE_103, explicit unsupported result)
 * 7. Invalid/null inputs validation
 * 8. Multiple independent cases without cross-contamination
 */
public class CaseSolverEngineTest {

    private CaseSolverEngine engine;
    private CaseContext defaultContext;

    @BeforeEach
    void setUp() {
        engine = new CaseSolverEngine(true); // Load default synthetic Case #103
        Set<String> discovered = new HashSet<>(Arrays.asList("E01", "E02", "E03", "E04", "E05", "E06", "E07", "E08"));
        Set<String> clues = new HashSet<>(Arrays.asList("C01", "C02", "C03", "C04", "C05", "C06"));
        Set<String> contradictions = new HashSet<>(Arrays.asList("K01", "K02", "K03"));
        defaultContext = new CaseContext("CASE_103", "THE POISONED DRINK", discovered, clues, contradictions);
    }

    @Test
    @DisplayName("Scenario 1: Correct suspect, motive and required evidence solves case with benchmark score 92")
    void testCorrectSuspectMotiveAndEvidence() {
        Set<String> evidence = new HashSet<>(Arrays.asList("E01", "E03", "E05"));
        DeductionRequest request = new DeductionRequest("CASE_103", "MEERA", "Financial dispute", evidence, "Followed financial audit trail");

        DeductionResult result = engine.evaluate(request, defaultContext);

        assertTrue(result.isSolved(), "Case should be solved for correct suspect, motive, and required evidence");
        assertEquals(92, result.getScore(), "Benchmark score should be 92 for solved Case #103");
        assertEquals("CASE SOLVED", result.getVerdictTitle());
        assertEquals("MASTER DETECTIVE", result.getRankTitle());
        assertEquals("SOLVED", result.getRecommendedCaseStatus());
        assertEquals(0, result.getWrongAccusations(), "Wrong accusations should be 0 when solved");
        assertTrue(result.getExplanation().contains("Meera"));
    }

    @Test
    @DisplayName("Scenario 2: Wrong suspect produces CASE UNRESOLVED with wrong accusation incremented")
    void testWrongSuspect() {
        Set<String> evidence = new HashSet<>(Arrays.asList("E01", "E03"));
        DeductionRequest request = new DeductionRequest("CASE_103", "ROHAN", "Financial dispute", evidence, "Staff suspicion");

        DeductionResult result = engine.evaluate(request, defaultContext);

        assertFalse(result.isSolved(), "Case should not be solved when accused suspect is wrong");
        assertEquals("CASE UNRESOLVED", result.getVerdictTitle());
        assertEquals(1, result.getWrongAccusations(), "Wrong accusations must be incremented to 1");
        assertTrue(result.getScore() <= 70, "Unsolved score must be capped at 70");
        assertEquals("IN_PROGRESS", result.getRecommendedCaseStatus());
    }

    @Test
    @DisplayName("Scenario 3: Wrong motive does not match (avoids overly broad matching)")
    void testWrongMotive() {
        Set<String> evidence = new HashSet<>(Arrays.asList("E01", "E03"));

        // Corporate blackmail
        DeductionRequest reqBlackmail = new DeductionRequest("CASE_103", "MEERA", "Corporate blackmail", evidence, "Blackmail theory");
        DeductionResult resBlackmail = engine.evaluate(reqBlackmail, defaultContext);
        assertFalse(resBlackmail.isSolved(), "Wrong motive 'Corporate blackmail' must not solve the case");
        assertEquals("CASE UNRESOLVED", resBlackmail.getVerdictTitle());

        // Personal vendetta
        DeductionRequest reqVendetta = new DeductionRequest("CASE_103", "MEERA", "Personal vendetta", evidence, "Vendetta theory");
        DeductionResult resVendetta = engine.evaluate(reqVendetta, defaultContext);
        assertFalse(resVendetta.isSolved(), "Wrong motive 'Personal vendetta' must not solve the case");

        // Motive with the word 'financial' but not matching expected motive exactly
        DeductionRequest reqBroad = new DeductionRequest("CASE_103", "MEERA", "Non-financial feud", evidence, "Different motive");
        DeductionResult resBroad = engine.evaluate(reqBroad, defaultContext);
        assertFalse(resBroad.isSolved(), "Broad motive containing 'financial' must NOT solve the case");
    }

    @Test
    @DisplayName("Scenario 4: Missing required evidence (only E01, missing E03) leaves case unresolved")
    void testMissingRequiredEvidence() {
        Set<String> evidence = new HashSet<>(Collections.singletonList("E01"));
        DeductionRequest request = new DeductionRequest("CASE_103", "MEERA", "Financial dispute", evidence, "Missing CCTV");

        DeductionResult result = engine.evaluate(request, defaultContext);

        assertFalse(result.isSolved(), "Case should remain unresolved if required evidence E03 is missing");
        assertEquals("CASE UNRESOLVED", result.getVerdictTitle());
        assertTrue(result.getScore() < 92, "Score should reflect partial progress below 92");
    }

    @Test
    @DisplayName("Scenario 5: Empty evidence selection leaves case unresolved")
    void testEmptyEvidenceSelection() {
        Set<String> emptyEvidence = Collections.emptySet();
        DeductionRequest request = new DeductionRequest("CASE_103", "MEERA", "Financial dispute", emptyEvidence, "No evidence selected");

        DeductionResult result = engine.evaluate(request, defaultContext);

        assertFalse(result.isSolved(), "Case cannot be solved with empty evidence selection");
        assertEquals("CASE UNRESOLVED", result.getVerdictTitle());
        assertTrue(result.getScore() <= 60, "Empty evidence score should reflect low confidence");
    }

    @Test
    @DisplayName("Scenario 6: Unknown case ID returns explicit unsupported result without fallback to CASE_103")
    void testUnknownCaseId() {
        Set<String> evidence = new HashSet<>(Arrays.asList("E01", "E03"));
        DeductionRequest request = new DeductionRequest("CASE_999", "MEERA", "Financial dispute", evidence, "Unknown case deduction");

        DeductionResult result = engine.evaluate(request, defaultContext);

        assertNotNull(result, "Result must not be null");
        assertFalse(result.isSolved(), "Unknown case must NOT be evaluated as solved");
        assertEquals(0, result.getScore(), "Unsupported case score must be 0");
        assertEquals("CASE UNRESOLVED", result.getVerdictTitle());
        assertEquals("UNSUPPORTED", result.getRecommendedCaseStatus());
        assertEquals("UNRANKED", result.getRankTitle());
        assertTrue(result.getExplanation().contains("No evaluation rules configured for case ID: CASE_999"),
                "Explanation must clearly indicate missing rules: " + result.getExplanation());

        // Verify documented exception variant
        assertThrows(IllegalArgumentException.class, () -> engine.evaluateStrict(request, defaultContext),
                "evaluateStrict must throw IllegalArgumentException for unknown case");
    }

    @Test
    @DisplayName("Scenario 7: Invalid and null inputs trigger documented IllegalArgumentException / NPE")
    void testInvalidOrNullInputs() {
        Set<String> validEvidence = new HashSet<>(Arrays.asList("E01", "E03"));

        // Null DeductionRequest
        assertThrows(IllegalArgumentException.class, () -> engine.evaluate(null, defaultContext),
                "Null request must throw IllegalArgumentException");

        // Null caseId in request constructor
        assertThrows(NullPointerException.class, () -> new DeductionRequest(null, "MEERA", "Motive", validEvidence, "Reason"));

        // Null suspectId in request constructor
        assertThrows(NullPointerException.class, () -> new DeductionRequest("CASE_103", null, "Motive", validEvidence, "Reason"));

        // Empty/whitespace suspectId evaluated by engine
        DeductionRequest emptySuspectReq = new DeductionRequest("CASE_103", "   ", "Motive", validEvidence, "Reason");
        assertThrows(IllegalArgumentException.class, () -> engine.evaluate(emptySuspectReq, defaultContext),
                "Empty suspectId must throw IllegalArgumentException");

        // Null motive evaluated by engine
        DeductionRequest nullMotiveReq = new DeductionRequest("CASE_103", "MEERA", null, validEvidence, "Reason");
        assertThrows(IllegalArgumentException.class, () -> engine.evaluate(nullMotiveReq, defaultContext),
                "Null/empty motive must throw IllegalArgumentException");

        // Empty whitespace motive evaluated by engine
        DeductionRequest emptyMotiveReq = new DeductionRequest("CASE_103", "MEERA", "   ", validEvidence, "Reason");
        assertThrows(IllegalArgumentException.class, () -> engine.evaluate(emptyMotiveReq, defaultContext),
                "Empty whitespace motive must throw IllegalArgumentException");

        // Null rule registration
        assertThrows(IllegalArgumentException.class, () -> engine.registerCaseRule(null),
                "Registering null rule must throw IllegalArgumentException");
    }

    @Test
    @DisplayName("Scenario 8: Multiple independent cases evaluate cleanly without cross-contamination")
    void testMultipleIndependentCases() {
        CaseSolverEngine cleanEngine = new CaseSolverEngine(false); // Clean engine without synthetic defaults

        // Case A: The Stolen Prototype
        cleanEngine.registerCaseRule(new CaseSolverEngine.CaseSolutionRule(
                "CASE_101", "ARJUN", "Corporate espionage",
                new HashSet<>(Arrays.asList("EV_SEC_LOG", "EV_USB")),
                "Arjun accessed the secure lab after hours.",
                "Insufficient links for Arjun.",
                5, 4, 2, 95
        ));

        // Case B: The Gallery Heist
        cleanEngine.registerCaseRule(new CaseSolverEngine.CaseSolutionRule(
                "CASE_102", "VIKTOR", "Art smuggling",
                new HashSet<>(Arrays.asList("EV_GLASS_CUTTER", "EV_ALIBI_FAIL")),
                "Viktor smuggled the painting via courier.",
                "Viktor's alibi holds.",
                7, 5, 3, 90
        ));

        // Solve Case A correctly
        DeductionRequest solveA = new DeductionRequest("CASE_101", "ARJUN", "Corporate espionage",
                new HashSet<>(Arrays.asList("EV_SEC_LOG", "EV_USB")), "Matched security log");
        DeductionResult resA = cleanEngine.evaluate(solveA, null);
        assertTrue(resA.isSolved(), "Case A must be solved");
        assertEquals(95, resA.getScore(), "Case A score should be 95");

        // Attempt Case B with Case A's culprit -> fails
        DeductionRequest failB = new DeductionRequest("CASE_102", "ARJUN", "Corporate espionage",
                new HashSet<>(Arrays.asList("EV_SEC_LOG", "EV_USB")), "Wrong suspect for Case B");
        DeductionResult resFailB = cleanEngine.evaluate(failB, null);
        assertFalse(resFailB.isSolved(), "Case B should fail with Case A's culprit");
        assertEquals(1, resFailB.getWrongAccusations());

        // Solve Case B correctly
        DeductionRequest solveB = new DeductionRequest("CASE_102", "VIKTOR", "Art smuggling",
                new HashSet<>(Arrays.asList("EV_GLASS_CUTTER", "EV_ALIBI_FAIL")), "Courier route confirmed");
        DeductionResult resB = cleanEngine.evaluate(solveB, null);
        assertTrue(resB.isSolved(), "Case B must be solved independently");
        assertEquals(90, resB.getScore(), "Case B score should be 90");
    }

    /**
     * Standalone main runner for direct CLI execution without Maven.
     */
    public static void main(String[] args) {
        CaseSolverEngineTest test = new CaseSolverEngineTest();
        int passed = 0;

        test.setUp();
        test.testCorrectSuspectMotiveAndEvidence();
        passed++;

        test.setUp();
        test.testWrongSuspect();
        passed++;

        test.setUp();
        test.testWrongMotive();
        passed++;

        test.setUp();
        test.testMissingRequiredEvidence();
        passed++;

        test.setUp();
        test.testEmptyEvidenceSelection();
        passed++;

        test.setUp();
        test.testUnknownCaseId();
        passed++;

        test.setUp();
        test.testInvalidOrNullInputs();
        passed++;

        test.setUp();
        test.testMultipleIndependentCases();
        passed++;

        System.out.println("CaseSolverEngineTest: ALL " + passed + " SCENARIOS PASSED!");
    }
}
