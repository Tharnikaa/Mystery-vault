package com.mysteryvault.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Model representing an investigation report generated for a case.
 * Owned by Member 3 (Module 10: Reports).
 */
public class InvestigationReport implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String reportId;
    private final String caseId;
    private final String caseTitle;
    private final LocalDateTime generatedAt;
    private final String summary;
    private final List<String> suspectSummaries;
    private final List<String> evidenceSummaries;
    private final List<String> clueSummaries;
    private final List<String> contradictions;
    private final String deductionConclusion;
    private final int confidenceScore; // 0-100

    public InvestigationReport(String reportId,
                               String caseId,
                               String caseTitle,
                               LocalDateTime generatedAt,
                               String summary,
                               List<String> suspectSummaries,
                               List<String> evidenceSummaries,
                               List<String> clueSummaries,
                               List<String> contradictions,
                               String deductionConclusion,
                               int confidenceScore) {
        this.reportId = Objects.requireNonNull(reportId, "reportId cannot be null");
        this.caseId = Objects.requireNonNull(caseId, "caseId cannot be null");
        this.caseTitle = caseTitle != null ? caseTitle : "Untitled Case";
        this.generatedAt = generatedAt != null ? generatedAt : LocalDateTime.now();
        this.summary = summary != null ? summary : "";
        this.suspectSummaries = suspectSummaries != null ? List.copyOf(suspectSummaries) : Collections.emptyList();
        this.evidenceSummaries = evidenceSummaries != null ? List.copyOf(evidenceSummaries) : Collections.emptyList();
        this.clueSummaries = clueSummaries != null ? List.copyOf(clueSummaries) : Collections.emptyList();
        this.contradictions = contradictions != null ? List.copyOf(contradictions) : Collections.emptyList();
        this.deductionConclusion = deductionConclusion != null ? deductionConclusion : "Pending Final Deduction";
        this.confidenceScore = Math.max(0, Math.min(100, confidenceScore));
    }

    public String getReportId() {
        return reportId;
    }

    public String getCaseId() {
        return caseId;
    }

    public String getCaseTitle() {
        return caseTitle;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public String getSummary() {
        return summary;
    }

    public List<String> getSuspectSummaries() {
        return suspectSummaries;
    }

    public List<String> getEvidenceSummaries() {
        return evidenceSummaries;
    }

    public List<String> getClueSummaries() {
        return clueSummaries;
    }

    public List<String> getContradictions() {
        return contradictions;
    }

    public String getDeductionConclusion() {
        return deductionConclusion;
    }

    public int getConfidenceScore() {
        return confidenceScore;
    }

    /**
     * Formats this report into a clean, human-readable dossier text.
     */
    public String toFormattedDossier() {
        StringBuilder sb = new StringBuilder();
        sb.append("================================================================================\n");
        sb.append("                      MYSTERY VAULT - OFFICIAL DOSSIER REPORT                   \n");
        sb.append("================================================================================\n");
        sb.append(String.format("Report ID: %-25s Case ID: %s\n", reportId, caseId));
        sb.append(String.format("Case Title: %-24s Date: %s\n", caseTitle, generatedAt));
        sb.append(String.format("Overall Confidence Score: %d / 100\n", confidenceScore));
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append("EXECUTIVE SUMMARY:\n");
        sb.append("  ").append(summary).append("\n\n");

        sb.append("SUSPECTS ON RECORD (").append(suspectSummaries.size()).append("):\n");
        if (suspectSummaries.isEmpty()) {
            sb.append("  [No suspects recorded]\n");
        } else {
            for (String s : suspectSummaries) {
                sb.append("  • ").append(s).append("\n");
            }
        }
        sb.append("\n");

        sb.append("CATALOGED EVIDENCE (").append(evidenceSummaries.size()).append("):\n");
        if (evidenceSummaries.isEmpty()) {
            sb.append("  [No evidence cataloged]\n");
        } else {
            for (String e : evidenceSummaries) {
                sb.append("  • ").append(e).append("\n");
            }
        }
        sb.append("\n");

        sb.append("KEY INVESTIGATION CLUES (").append(clueSummaries.size()).append("):\n");
        if (clueSummaries.isEmpty()) {
            sb.append("  [No clues resolved]\n");
        } else {
            for (String c : clueSummaries) {
                sb.append("  • ").append(c).append("\n");
            }
        }
        sb.append("\n");

        sb.append("TIMELINE & STATEMENT CONTRADICTIONS (").append(contradictions.size()).append("):\n");
        if (contradictions.isEmpty()) {
            sb.append("  [No contradictions flagged]\n");
        } else {
            for (String con : contradictions) {
                sb.append("  [!] ").append(con).append("\n");
            }
        }
        sb.append("\n");

        sb.append("FINAL DEDUCTION CONCLUSION:\n");
        sb.append("  ").append(deductionConclusion).append("\n");
        sb.append("================================================================================\n");
        return sb.toString();
    }

    @Override
    public String toString() {
        return "InvestigationReport{" +
                "reportId='" + reportId + '\'' +
                ", caseId='" + caseId + '\'' +
                ", caseTitle='" + caseTitle + '\'' +
                ", confidenceScore=" + confidenceScore +
                '}';
    }
}
