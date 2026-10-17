package com.mysteryvault.service;

import com.mysteryvault.service.dto.CaseContext;
import com.mysteryvault.service.dto.DeductionRequest;
import com.mysteryvault.service.dto.DeductionResult;

import java.util.*;

/**
 * Module 11 — Final Deduction / Case Solver Engine
 *
 * Owned by Member 4.
 * Evaluates player deduction submissions against case evidence, motive, and suspect claims.
 * Computes scores and explanations without directly mutating repositories or touching Swing UI.
 */
public class CaseSolverEngine {

    public static class CaseSolutionRule {
        private final String caseId;
        private final String culpritId;
        private final String expectedMotive;
        private final Set<String> requiredEvidenceIds;
        private final String successExplanation;
        private final String failureExplanation;

        public CaseSolutionRule(String caseId, String culpritId, String expectedMotive,
                                Set<String> requiredEvidenceIds, String successExplanation, String failureExplanation) {
            this.caseId = caseId;
            this.culpritId = culpritId;
            this.expectedMotive = expectedMotive;
            this.requiredEvidenceIds = requiredEvidenceIds != null ? new HashSet<>(requiredEvidenceIds) : Collections.emptySet();
            this.successExplanation = successExplanation;
            this.failureExplanation = failureExplanation;
        }

        public String getCaseId() { return caseId; }
        public String getCulpritId() { return culpritId; }
        public String getExpectedMotive() { return expectedMotive; }
        public Set<String> getRequiredEvidenceIds() { return requiredEvidenceIds; }
        public String getSuccessExplanation() { return successExplanation; }
        public String getFailureExplanation() { return failureExplanation; }
    }

    private final Map<String, CaseSolutionRule> caseRules = new HashMap<>();

    public CaseSolverEngine() {
        registerDefaultSyntheticRules();
    }

    private void registerDefaultSyntheticRules() {
        // Default synthetic rule for Case #103: The Poisoned Drink
        Set<String> case103Evidence = new HashSet<>(Arrays.asList("E01", "E03"));
        registerCaseRule(new CaseSolutionRule(
                "CASE_103",
                "MEERA",
                "Financial dispute",
                case103Evidence,
                "Forensic toxin analysis on the wine glass (E01) combined with CCTV footage (E03) proves Meera had sole access to the glass during the critical 8:45 PM timeline window, motivated by the audit dispute.",
                "The presented evidence does not substantiate the accusation. There are insufficient physical links or unverified alibis."
        ));
    }

    public void registerCaseRule(CaseSolutionRule rule) {
        if (rule != null && rule.getCaseId() != null) {
            caseRules.put(rule.getCaseId(), rule);
        }
    }

    /**
     * Evaluates a deduction request against the case context and rules.
     *
     * @param request Player deduction input
     * @param context Active case context
     * @return Structured evaluation result
     */
    public DeductionResult evaluate(DeductionRequest request, CaseContext context) {
        if (request == null) {
            throw new IllegalArgumentException("DeductionRequest cannot be null");
        }

        String caseId = request.getCaseId();
        CaseSolutionRule rule = caseRules.get(caseId);

        // Fallback for cases without explicit rules
        if (rule == null) {
            rule = caseRules.get("CASE_103");
        }

        boolean suspectCorrect = rule != null && rule.getCulpritId().equalsIgnoreCase(request.getAccusedSuspectId());
        boolean motiveCorrect = rule != null && (rule.getExpectedMotive().equalsIgnoreCase(request.getMotive())
                || request.getMotive().toLowerCase().contains("financial"));

        int matchedEvidenceCount = 0;
        if (rule != null) {
            for (String req : rule.getRequiredEvidenceIds()) {
                if (request.getSupportingEvidenceIds().contains(req)) {
                    matchedEvidenceCount++;
                }
            }
        }

        boolean evidenceSufficient = rule == null || matchedEvidenceCount >= rule.getRequiredEvidenceIds().size();
        boolean solved = suspectCorrect && motiveCorrect && evidenceSufficient;

        int score;
        String rank;
        String verdictTitle;
        String explanation;
        int wrongAccusations;

        if (solved) {
            score = 92;
            rank = "MASTER DETECTIVE";
            verdictTitle = "CASE SOLVED";
            explanation = rule != null ? rule.getSuccessExplanation() : "Case successfully resolved.";
            wrongAccusations = 0;
        } else {
            // Partial score based on partial deductions
            int baseScore = 20;
            if (suspectCorrect) baseScore += 30;
            if (motiveCorrect) baseScore += 20;
            baseScore += matchedEvidenceCount * 10;
            score = Math.min(baseScore, 70);
            rank = "SENIOR DETECTIVE";
            verdictTitle = "CASE UNRESOLVED";
            explanation = rule != null ? rule.getFailureExplanation() : "Key contradictions remain unexplained.";
            wrongAccusations = suspectCorrect ? 0 : 1;
        }

        String caseTitle = context != null ? context.getCaseTitle() : "THE POISONED DRINK";

        return new DeductionResult(
                solved,
                score,
                verdictTitle,
                caseTitle,
                explanation,
                solved ? "SOLVED" : "IN_PROGRESS",
                8, 8,
                6, 6,
                3, 3,
                wrongAccusations,
                rank
        );
    }
}
