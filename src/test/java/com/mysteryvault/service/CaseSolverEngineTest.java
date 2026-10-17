package com.mysteryvault.service;

import com.mysteryvault.service.dto.CaseContext;
import com.mysteryvault.service.dto.DeductionRequest;
import com.mysteryvault.service.dto.DeductionResult;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CaseSolverEngineTest {

    public static void main(String[] args) {
        testCorrectDeduction();
        testIncorrectSuspect();
        System.out.println("All CaseSolverEngine tests passed successfully!");
    }

    private static void testCorrectDeduction() {
        CaseSolverEngine engine = new CaseSolverEngine();
        Set<String> evidence = new HashSet<>(Arrays.asList("E01", "E03", "E05"));
        DeductionRequest req = new DeductionRequest("CASE_103", "MEERA", "Financial dispute", evidence, "Followed the paper trail");
        CaseContext ctx = new CaseContext("CASE_103", "THE POISONED DRINK", evidence, new HashSet<>(), new HashSet<>());

        DeductionResult result = engine.evaluate(req, ctx);
        if (!result.isSolved()) {
            throw new AssertionError("Expected case to be solved");
        }
        if (result.getScore() != 92) {
            throw new AssertionError("Expected score 92, got " + result.getScore());
        }
        System.out.println("testCorrectDeduction: PASSED (Score: " + result.getScore() + ")");
    }

    private static void testIncorrectSuspect() {
        CaseSolverEngine engine = new CaseSolverEngine();
        Set<String> evidence = new HashSet<>(Arrays.asList("E01", "E03"));
        DeductionRequest req = new DeductionRequest("CASE_103", "ROHAN", "Accidental", evidence, "Kitchen proximity");
        CaseContext ctx = new CaseContext("CASE_103", "THE POISONED DRINK", evidence, new HashSet<>(), new HashSet<>());

        DeductionResult result = engine.evaluate(req, ctx);
        if (result.isSolved()) {
            throw new AssertionError("Expected case to NOT be solved for wrong suspect");
        }
        if (result.getWrongAccusations() != 1) {
            throw new AssertionError("Expected 1 wrong accusation, got " + result.getWrongAccusations());
        }
        System.out.println("testIncorrectSuspect: PASSED (Score: " + result.getScore() + ", Wrong: " + result.getWrongAccusations() + ")");
    }
}
