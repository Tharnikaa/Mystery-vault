package com.mysteryvault.ui.swing.viewmodel;

public class AnalysisViewModel {
    private final int timelineProgress;
    private final int evidenceProgress;
    private final int statementProgress;
    private final int connectionProgress;
    private final int connectionsFound;
    private final int contradictions;
    private final int importantEvidence;
    private final int suspiciousCharacters;

    public AnalysisViewModel(int timelineProgress, int evidenceProgress, int statementProgress, int connectionProgress,
                             int connectionsFound, int contradictions, int importantEvidence, int suspiciousCharacters) {
        this.timelineProgress = timelineProgress;
        this.evidenceProgress = evidenceProgress;
        this.statementProgress = statementProgress;
        this.connectionProgress = connectionProgress;
        this.connectionsFound = connectionsFound;
        this.contradictions = contradictions;
        this.importantEvidence = importantEvidence;
        this.suspiciousCharacters = suspiciousCharacters;
    }

    public int getTimelineProgress() { return timelineProgress; }
    public int getEvidenceProgress() { return evidenceProgress; }
    public int getStatementProgress() { return statementProgress; }
    public int getConnectionProgress() { return connectionProgress; }
    public int getConnectionsFound() { return connectionsFound; }
    public int getContradictions() { return contradictions; }
    public int getImportantEvidence() { return importantEvidence; }
    public int getSuspiciousCharacters() { return suspiciousCharacters; }
}
