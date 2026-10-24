package com.mysteryvault.analyzer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TimelineAnalyzerTask implements Analyzer {

    @Override
    public AnalysisResult analyze(String caseId, Connection conn) throws Exception {
        int totalEvents = 0;
        int verifiedEvents = 0;
        int conflictsFound = 0;
        List<String> findings = new ArrayList<>();

        String query = "SELECT event_id, timestamp_str, description, has_conflict, conflict_details FROM timeline_events ORDER BY sort_order";
        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                totalEvents++;
                int conflict = rs.getInt("has_conflict");
                if (conflict == 1) {
                    conflictsFound++;
                    String details = rs.getString("conflict_details");
                    findings.add("Timeline Discrepancy at " + rs.getString("timestamp_str") + ": " + (details != null ? details : rs.getString("description")));
                } else {
                    verifiedEvents++;
                }
            }
        }

        int progress = totalEvents > 0 ? (verifiedEvents * 100) / totalEvents : 100;
        return new AnalysisResult("Timeline Analysis", progress, conflictsFound, findings);
    }
}
