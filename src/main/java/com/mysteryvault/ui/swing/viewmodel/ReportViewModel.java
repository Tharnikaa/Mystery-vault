package com.mysteryvault.ui.swing.viewmodel;

public class ReportViewModel {
    private final String caseId;
    private final String caseTitle;
    private final String culpritName;
    private final String motive;
    private final String status;
    private final String officialSummary;

    public ReportViewModel(String caseId, String caseTitle, String culpritName, String motive, String status, String officialSummary) {
        this.caseId = caseId;
        this.caseTitle = caseTitle;
        this.culpritName = culpritName;
        this.motive = motive;
        this.status = status;
        this.officialSummary = officialSummary;
    }

    public String getCaseId() { return caseId; }
    public String getCaseTitle() { return caseTitle; }
    public String getCulpritName() { return culpritName; }
    public String getMotive() { return motive; }
    public String getStatus() { return status; }
    public String getOfficialSummary() { return officialSummary; }
}
