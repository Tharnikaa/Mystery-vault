package com.mysteryvault.analyzer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class EvidenceAnalyzerTask implements Analyzer {

    @Override
    public AnalysisResult analyze(String caseId, Connection conn) throws Exception {
        int totalEvidence = 0;
        int discoveredCount = 0;
        int criticalCount = 0;
        List<String> findings = new ArrayList<>();

        String query = "SELECT evidence_id, name, significance, is_discovered FROM evidence";
        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                totalEvidence++;
                int disc = rs.getInt("is_discovered");
                String sig = rs.getString("significance");
                if (disc == 1) {
                    discoveredCount++;
                }
                if ("CRITICAL".equalsIgnoreCase(sig) || "HIGH".equalsIgnoreCase(sig)) {
                    criticalCount++;
                    findings.add("Decisive Evidence cataloged: " + rs.getString("name"));
                }
            }
        }

        int progress = totalEvidence > 0 ? (discoveredCount * 100) / totalEvidence : 100;
        return new AnalysisResult("Evidence Analysis", progress, criticalCount, findings);
    }
}
