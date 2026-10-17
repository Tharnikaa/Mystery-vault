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
 * Computes scores, ranks, and explanations without directly mutating repositories or touching Swing UI.
 *
 * Scoring Formula Documentation:
 * ------------------------------
 * Total maximum score: Configurable per CaseSolutionRule (default 92 for Case #103 demo benchmark, max 100).
 *
 * 1. Solved Condition:
 *    A deduction is considered SOLVED if and only if ALL of the following criteria are met:
 *    - Suspect Match: The accused suspect matches the rule's culprit ID (case-insensitive, trimmed).
 *    - Motive Match: The submitted motive exactly matches the expected motive (case-insensitive, trimmed).
 *    - Evidence Sufficiency: All required evidence IDs specified by the rule are present in the submitted supporting evidence.
 *
 * 2. Solved Scoring:
 *    - Score: Configured rule max score (defaults to 92 for Case #103).
 *    - Rank: "MASTER DETECTIVE"
 *    - Verdict: "CASE SOLVED"
 *    - Wrong Accusations: 0
 *    - Status: "SOLVED"
 *    - Explanation: Rule's success explanation.
 *
 * 3. Unresolved (Partial) Scoring:
 *    - Base score: 10
 *    - Suspect Correct: +30 points
 *    - Motive Correct: +20 points
 *    - Matched Required Evidence: Proportional up to +20 points (e.g. (matchedCount / totalRequired) * 20).
 *    - Ceiling: Capped strictly at 70 points to prevent unverified deductions from masquerading as solved cases.
 *    - Rank Assignment:
 *        * Score >= 75: "SENIOR DETECTIVE"
 *        * Score >= 50: "DETECTIVE INSPECTOR"
 *        * Score >= 30: "JUNIOR DETECTIVE"
 *        * Score <  30: "ROOKIE INVESTIGATOR"
 *    - Verdict: "CASE UNRESOLVED"
 *    - Wrong Accusations: 1 if the accused suspect is incorrect; 0 if the suspect is correct but motive/evidence is incomplete.
 *    - Status: "IN_PROGRESS"
 *    - Explanation: Rule's failure explanation.
 *
 * 4. Unsupported / Unknown Case Evaluation:
 *    - Evaluated without fallback to synthetic cases.
 *    - Score: 0
 *    - Rank: "UNRANKED"
 *    - Verdict: "CASE UNRESOLVED"
 *    - Status: "UNSUPPORTED"
 *    - Wrong Accusations: 0
 *    - Explanation: Explicit message stating no evaluation rules are configured for the case ID.
 *
 * Investigation Statistics:
 * -------------------------
 * Derived dynamically from CaseContext and CaseSolutionRule instead of hardcoding static counts:
 * - Evidence Found: Discovered evidence count from context, or count of submitted supporting evidence.
 * - Total Evidence: Rule total evidence count (or max of discovered and rule count).
 * - Important Clues: Discovered clues count from context, or scaled proportionally for unresolved cases.
 * - Total Clues: Rule total clues count.
 * - Contradictions Found: Uncovered contradictions count from context, or scaled proportionally.
 * - Total Contradictions: Rule total contradictions count.
 */
public class CaseSolverEngine {

    /**
     * Configurable solution rule defining criteria and metadata for evaluating deductions for a specific case.
     */
    public static class CaseSolutionRule {
        private final String caseId;
        private final String culpritId;
        private final String expectedMotive;
        private final Set<String> requiredEvidenceIds;
        private final String successExplanation;
        private final String failureExplanation;
        private final int totalEvidenceCount;
        private final int totalCluesCount;
        private final int totalContradictions;
        private final int maxScore;

        public CaseSolutionRule(String caseId, String culpritId, String expectedMotive,
                                Set<String> requiredEvidenceIds, String successExplanation, String failureExplanation) {
            this(caseId, culpritId, expectedMotive, requiredEvidenceIds, successExplanation, failureExplanation, 8, 6, 3, 92);
        }

        public CaseSolutionRule(String caseId, String culpritId, String expectedMotive,
                                Set<String> requiredEvidenceIds, String successExplanation, String failureExplanation,
                                int totalEvidenceCount, int totalCluesCount, int totalContradictions, int maxScore) {
            if (caseId == null || caseId.trim().isEmpty()) {
                throw new IllegalArgumentException("Case ID cannot be null or empty");
            }
            if (culpritId == null || culpritId.trim().isEmpty()) {
                throw new IllegalArgumentException("Culprit ID cannot be null or empty");
            }
            if (expectedMotive == null || expectedMotive.trim().isEmpty()) {
                throw new IllegalArgumentException("Expected motive cannot be null or empty");
            }
            this.caseId = caseId.trim();
            this.culpritId = culpritId.trim();
            this.expectedMotive = expectedMotive.trim();
            this.requiredEvidenceIds = requiredEvidenceIds != null
                    ? Collections.unmodifiableSet(new HashSet<>(requiredEvidenceIds))
                    : Collections.emptySet();
            this.successExplanation = successExplanation != null ? successExplanation : "Case successfully resolved.";
            this.failureExplanation = failureExplanation != null ? failureExplanation : "The presented evidence does not substantiate the accusation.";
            this.totalEvidenceCount = totalEvidenceCount;
            this.totalCluesCount = totalCluesCount;
            this.totalContradictions = totalContradictions;
            this.maxScore = maxScore;
        }

        public String getCaseId() { return caseId; }
        public String getCulpritId() { return culpritId; }
        public String getExpectedMotive() { return expectedMotive; }
        public Set<String> getRequiredEvidenceIds() { return requiredEvidenceIds; }
        public String getSuccessExplanation() { return successExplanation; }
        public String getFailureExplanation() { return failureExplanation; }
        public int getTotalEvidenceCount() { return totalEvidenceCount; }
        public int getTotalCluesCount() { return totalCluesCount; }
        public int getTotalContradictions() { return totalContradictions; }
        public int getMaxScore() { return maxScore; }
    }

    private final Map<String, CaseSolutionRule> caseRules = new LinkedHashMap<>();

    /**
     * Constructs a solver engine initialized with synthetic default rules for mock/demo use (Case #103).
     */
    public CaseSolverEngine() {
        this(true);
    }

    /**
     * Constructs a solver engine with option to load or omit synthetic default rules.
     *
     * @param loadDefaultSyntheticRules true to register default Case #103 mock rules; false for clean standalone configuration
     */
    public CaseSolverEngine(boolean loadDefaultSyntheticRules) {
        if (loadDefaultSyntheticRules) {
            registerDefaultSyntheticRules();
        }
    }

    /**
     * Registers default synthetic rule for Case #103: The Poisoned Drink (Mock/Demo only).
     */
    public void registerDefaultSyntheticRules() {
        Set<String> case103Evidence = new HashSet<>(Arrays.asList("E01", "E03"));
        registerCaseRule(new CaseSolutionRule(
                "CASE_103",
                "MEERA",
                "Financial dispute",
                case103Evidence,
                "Forensic toxin analysis on the wine glass (E01) combined with CCTV footage (E03) proves Meera had sole access to the glass during the critical 8:45 PM timeline window, motivated by the audit dispute.",
                "The presented evidence does not substantiate the accusation. There are insufficient physical links or unverified alibis.",
                8,
                6,
                3,
                92
        ));
    }

    /**
     * Registers or updates a case solution rule.
     *
     * @param rule Configured case solution rule
     */
    public void registerCaseRule(CaseSolutionRule rule) {
        if (rule == null) {
            throw new IllegalArgumentException("CaseSolutionRule cannot be null");
        }
        caseRules.put(rule.getCaseId(), rule);
    }

    public boolean hasCaseRule(String caseId) {
        return caseId != null && caseRules.containsKey(caseId.trim());
    }

    public CaseSolutionRule getCaseRule(String caseId) {
        return caseId != null ? caseRules.get(caseId.trim()) : null;
    }

    public CaseSolutionRule removeCaseRule(String caseId) {
        return caseId != null ? caseRules.remove(caseId.trim()) : null;
    }

    public void clearCaseRules() {
        caseRules.clear();
    }

    /**
     * Evaluates a player deduction submission against registered case rules.
     *
     * @param request Player deduction input
     * @param context Active case context containing investigation state
     * @return Structured evaluation result; returns unsupported result if case ID is unknown
     * @throws IllegalArgumentException if request is null, or required fields (caseId, suspect, motive, evidence set) are null/empty
     */
    public DeductionResult evaluate(DeductionRequest request, CaseContext context) {
        validateRequest(request);

        String caseId = request.getCaseId().trim();
        CaseSolutionRule rule = caseRules.get(caseId);

        // Explicit unsupported-case result without falling back to CASE_103
        if (rule == null) {
            String title = (context != null && context.getCaseTitle() != null) ? context.getCaseTitle() : caseId;
            return new DeductionResult(
                    false,
                    0,
                    "CASE UNRESOLVED",
                    title,
                    "No evaluation rules configured for case ID: " + caseId + ". Please register a valid CaseSolutionRule.",
                    "UNSUPPORTED",
                    0, 0,
                    0, 0,
                    0, 0,
                    0,
                    "UNRANKED"
            );
        }

        return evaluateAgainstRule(request, context, rule);
    }

    /**
     * Strict evaluation variant that throws an exception if the case ID is unknown.
     *
     * @param request Player deduction input
     * @param context Active case context
     * @return DeductionResult on success
     * @throws IllegalArgumentException if request is invalid or case ID has no registered rules
     */
    public DeductionResult evaluateStrict(DeductionRequest request, CaseContext context) {
        validateRequest(request);

        String caseId = request.getCaseId().trim();
        CaseSolutionRule rule = caseRules.get(caseId);
        if (rule == null) {
            throw new IllegalArgumentException("Unknown or unsupported case ID: " + caseId);
        }

        return evaluateAgainstRule(request, context, rule);
    }

    private void validateRequest(DeductionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("DeductionRequest cannot be null");
        }
        if (request.getCaseId() == null || request.getCaseId().trim().isEmpty()) {
            throw new IllegalArgumentException("Case ID cannot be null or empty");
        }
        if (request.getAccusedSuspectId() == null || request.getAccusedSuspectId().trim().isEmpty()) {
            throw new IllegalArgumentException("Accused suspect ID cannot be null or empty");
        }
        if (request.getMotive() == null || request.getMotive().trim().isEmpty()) {
            throw new IllegalArgumentException("Motive cannot be null or empty");
        }
        if (request.getSupportingEvidenceIds() == null) {
            throw new IllegalArgumentException("Supporting evidence set cannot be null");
        }
    }

    private DeductionResult evaluateAgainstRule(DeductionRequest request, CaseContext context, CaseSolutionRule rule) {
        // Suspect matching: case-insensitive, trimmed
        boolean suspectCorrect = rule.getCulpritId().equalsIgnoreCase(request.getAccusedSuspectId().trim());

        // Motive matching: exact normalized match without overly broad substring checks
        boolean motiveCorrect = rule.getExpectedMotive().equalsIgnoreCase(request.getMotive().trim());

        // Evidence sufficiency: checks required evidence presence
        Set<String> submittedEvidence = request.getSupportingEvidenceIds();
        Set<String> requiredEvidence = rule.getRequiredEvidenceIds();

        int matchedEvidenceCount = 0;
        for (String reqId : requiredEvidence) {
            if (submittedEvidence.contains(reqId)) {
                matchedEvidenceCount++;
            }
        }

        boolean evidenceSufficient = !requiredEvidence.isEmpty() && matchedEvidenceCount >= requiredEvidence.size();
        boolean solved = suspectCorrect && motiveCorrect && evidenceSufficient;

        int score;
        String rank;
        String verdictTitle;
        String explanation;
        int wrongAccusations;

        if (solved) {
            score = rule.getMaxScore();
            rank = "MASTER DETECTIVE";
            verdictTitle = "CASE SOLVED";
            explanation = rule.getSuccessExplanation();
            wrongAccusations = 0;
        } else {
            // Partial score formula
            int baseScore = 10;
            if (suspectCorrect) baseScore += 30;
            if (motiveCorrect) baseScore += 20;
            if (!requiredEvidence.isEmpty()) {
                baseScore += (int) Math.round(((double) matchedEvidenceCount / requiredEvidence.size()) * 20.0);
            }
            score = Math.min(baseScore, 70);

            if (score >= 75) {
                rank = "SENIOR DETECTIVE";
            } else if (score >= 50) {
                rank = "DETECTIVE INSPECTOR";
            } else if (score >= 30) {
                rank = "JUNIOR DETECTIVE";
            } else {
                rank = "ROOKIE INVESTIGATOR";
            }

            verdictTitle = "CASE UNRESOLVED";
            explanation = rule.getFailureExplanation();
            wrongAccusations = suspectCorrect ? 0 : 1;
        }

        // Compute dynamic, non-misleading investigation statistics
        int evidenceFound;
        int totalEvidence = rule.getTotalEvidenceCount();
        if (context != null && !context.getDiscoveredEvidenceIds().isEmpty()) {
            evidenceFound = context.getDiscoveredEvidenceIds().size();
            totalEvidence = Math.max(totalEvidence, evidenceFound);
        } else {
            evidenceFound = submittedEvidence.size();
        }

        int cluesFound;
        int totalClues = rule.getTotalCluesCount();
        if (context != null && !context.getDiscoveredClueIds().isEmpty()) {
            cluesFound = context.getDiscoveredClueIds().size();
            totalClues = Math.max(totalClues, cluesFound);
        } else {
            cluesFound = solved ? totalClues : Math.min(totalClues, matchedEvidenceCount * 2);
        }

        int contradictionsFound;
        int totalContradictions = rule.getTotalContradictions();
        if (context != null && !context.getUncoveredContradictionIds().isEmpty()) {
            contradictionsFound = context.getUncoveredContradictionIds().size();
            totalContradictions = Math.max(totalContradictions, contradictionsFound);
        } else {
            contradictionsFound = solved ? totalContradictions : (motiveCorrect ? 2 : 1);
        }

        String caseTitle = (context != null && context.getCaseTitle() != null)
                ? context.getCaseTitle()
                : ("CASE #" + rule.getCaseId());

        return new DeductionResult(
                solved,
                score,
                verdictTitle,
                caseTitle,
                explanation,
                solved ? "SOLVED" : "IN_PROGRESS",
                evidenceFound, totalEvidence,
                cluesFound, totalClues,
                contradictionsFound, totalContradictions,
                wrongAccusations,
                rank
        );
    }
}
