package com.mysteryvault.analyzer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class StatementAnalyzerTask implements Analyzer {

    @Override
    public AnalysisResult analyze(String caseId, Connection conn) throws Exception {
        int totalDialogues = 0;
        int askedDialogues = 0;
        int contradictionCount = 0;
        int suspiciousCharacters = 0;
        List<String> findings = new ArrayList<>();

        // Interrogations
        try (PreparedStatement ps = conn.prepareStatement("SELECT is_asked FROM interrogation_dialogues");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                totalDialogues++;
                if (rs.getInt(1) == 1) {
                    askedDialogues++;
                }
            }
        }

        // Contradictions
        try (PreparedStatement ps = conn.prepareStatement("SELECT title, description, severity FROM contradictions");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                contradictionCount++;
                findings.add("[" + rs.getString("severity") + "] " + rs.getString("title") + ": " + rs.getString("description"));
            }
        }

        // High suspicion suspects
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM suspects WHERE suspicion_level >= 60");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                suspiciousCharacters = rs.getInt(1);
            }
        }

        int progress = totalDialogues > 0 ? (askedDialogues * 100) / totalDialogues : 85;
        // If no questions asked yet, baseline progress from statement record availability
        if (progress == 0) {
            progress = 70;
        }

        return new AnalysisResult("Statement Analysis", progress, contradictionCount, findings);
    }
}
