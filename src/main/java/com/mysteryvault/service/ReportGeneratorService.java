package com.mysteryvault.service;

import com.mysteryvault.model.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service that compiles case, evidence, suspects, clues, and deduction into an InvestigationReport.
 * Owned by Member 3 (Module 10: Reports).
 */
public class ReportGeneratorService {

    public ReportGeneratorService() {
    }

    /**
     * Generates a complete dossier report using available domain data.
     */
    public InvestigationReport generateReport(Case caseEntity,
                                              List<Suspect> suspects,
                                              List<Evidence> evidenceList,
                                              List<Clue> clues,
                                              List<String> contradictions,
                                              String deductionConclusion,
                                              int confidenceScore) {
        Objects.requireNonNull(caseEntity, "caseEntity cannot be null");

        String reportId = "REP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String caseTitle = caseEntity.getTitle();
        String caseId = caseEntity.getId();

        String summary = String.format("Official investigation summary for Case '%s' (ID: %s). Current status: %s, Priority: %s.",
                caseTitle, caseId, caseEntity.getStatus(), caseEntity.getPriority());

        List<String> suspectSummaries = new ArrayList<>();
        if (suspects != null) {
            for (Suspect s : suspects) {
                suspectSummaries.add(String.format("%s (ID: %s, Age: %d) - %s | Suspicion: %d%% | Alibi: %s [Verified: %s]",
                        s.getName(), s.getId(), s.getAge(), s.getOccupation(), s.getSuspicionScore(), s.getAlibi(), s.isAlibiVerified()));
            }
        }

        List<String> evidenceSummaries = new ArrayList<>();
        if (evidenceList != null) {
            for (Evidence e : evidenceList) {
                evidenceSummaries.add(e.getSummary());
            }
        }

        List<String> clueSummaries = new ArrayList<>();
        if (clues != null) {
            for (Clue c : clues) {
                clueSummaries.add(String.format("Clue '%s' (ID: %s) [Unlocked: %s] - Requires Evidence: %s",
                        c.getTitle(), c.getId(), c.isUnlocked(), c.getRequiredEvidenceIds()));
            }
        }

        List<String> resolvedContradictions = contradictions != null ? List.copyOf(contradictions) : Collections.emptyList();

        return new InvestigationReport(
                reportId,
                caseId,
                caseTitle,
                LocalDateTime.now(),
                summary,
                suspectSummaries,
                evidenceSummaries,
                clueSummaries,
                resolvedContradictions,
                deductionConclusion,
                confidenceScore
        );
    }
}
