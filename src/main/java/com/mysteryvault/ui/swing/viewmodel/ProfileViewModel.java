package com.mysteryvault.ui.swing.viewmodel;

public class ProfileViewModel {
    private final String rank;
    private final int xp;
    private final int casesSolved;
    private final int totalCases;
    private final int accuracyPercentage;
    private final int casesAttempted;
    private final int bestScore;

    public ProfileViewModel(String rank, int xp, int casesSolved, int totalCases, int accuracyPercentage, int casesAttempted, int bestScore) {
        this.rank = rank;
        this.xp = xp;
        this.casesSolved = casesSolved;
        this.totalCases = totalCases;
        this.accuracyPercentage = accuracyPercentage;
        this.casesAttempted = casesAttempted;
        this.bestScore = bestScore;
    }

    public String getRank() { return rank; }
    public int getXp() { return xp; }
    public int getCasesSolved() { return casesSolved; }
    public int getTotalCases() { return totalCases; }
    public int getAccuracyPercentage() { return accuracyPercentage; }
    public int getCasesAttempted() { return casesAttempted; }
    public int getBestScore() { return bestScore; }

    public String getAccuracyDisplay() {
        return casesAttempted > 0 ? accuracyPercentage + "%" : "N/A";
    }
}
