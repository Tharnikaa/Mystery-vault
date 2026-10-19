package com.mysteryvault.service.dto;

import java.io.Serializable;

/**
 * Module 11 DTO representing the evaluation result produced by CaseSolverEngine.
 */
public class DeductionResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean solved;
    private final int score;
    private final String verdictTitle;
    private final String caseTitle;
    private final String explanation;
    private final String recommendedCaseStatus;
    private final int evidenceFoundCount;
    private final int totalEvidenceCount;
    private final int importantCluesCount;
    private final int totalCluesCount;
    private final int contradictionsFound;
    private final int totalContradictions;
    private final int wrongAccusations;
    private final String rankTitle;

    public DeductionResult(boolean solved, int score, String verdictTitle, String caseTitle,
                           String explanation, String recommendedCaseStatus,
                           int evidenceFoundCount, int totalEvidenceCount,
                           int importantCluesCount, int totalCluesCount,
                           int contradictionsFound, int totalContradictions,
                           int wrongAccusations, String rankTitle) {
        this.solved = solved;
        this.score = score;
        this.verdictTitle = verdictTitle != null ? verdictTitle : (solved ? "CASE SOLVED" : "CASE UNRESOLVED");
        this.caseTitle = caseTitle != null ? caseTitle : "THE POISONED DRINK";
        this.explanation = explanation != null ? explanation : "";
        this.recommendedCaseStatus = recommendedCaseStatus != null ? recommendedCaseStatus : (solved ? "SOLVED" : "IN_PROGRESS");
        this.evidenceFoundCount = evidenceFoundCount;
        this.totalEvidenceCount = totalEvidenceCount;
        this.importantCluesCount = importantCluesCount;
        this.totalCluesCount = totalCluesCount;
        this.contradictionsFound = contradictionsFound;
        this.totalContradictions = totalContradictions;
        this.wrongAccusations = wrongAccusations;
        this.rankTitle = rankTitle != null ? rankTitle : "MASTER DETECTIVE";
    }

    public boolean isSolved() {
        return solved;
    }

    public int getScore() {
        return score;
    }

    public String getVerdictTitle() {
        return verdictTitle;
    }

    public String getCaseTitle() {
        return caseTitle;
    }

    public String getExplanation() {
        return explanation;
    }

    public String getRecommendedCaseStatus() {
        return recommendedCaseStatus;
    }

    public int getEvidenceFoundCount() {
        return evidenceFoundCount;
    }

    public int getTotalEvidenceCount() {
        return totalEvidenceCount;
    }

    public int getImportantCluesCount() {
        return importantCluesCount;
    }

    public int getTotalCluesCount() {
        return totalCluesCount;
    }

    public int getContradictionsFound() {
        return contradictionsFound;
    }

    public int getTotalContradictions() {
        return totalContradictions;
    }

    public int getWrongAccusations() {
        return wrongAccusations;
    }

    public String getRankTitle() {
        return rankTitle;
    }

    @Override
    public String toString() {
        return "DeductionResult{" +
                "solved=" + solved +
                ", score=" + score +
                ", verdictTitle='" + verdictTitle + '\'' +
                ", recommendedCaseStatus='" + recommendedCaseStatus + '\'' +
                '}';
    }
}
