package com.mysteryvault.analyzer;

import com.mysteryvault.model.DigitalEvidence;
import com.mysteryvault.model.DocumentEvidence;
import com.mysteryvault.model.Evidence;

import java.util.*;

/**
 * Task that inspects physical, document, and digital evidence.
 * Owned by Member 3 (Module 8: Concurrent Analysis).
 */
public class EvidenceAnalyzerTask implements Analyzer {

    private final List<Evidence> evidencePool;

    public EvidenceAnalyzerTask(List<Evidence> evidencePool) {
        this.evidencePool = evidencePool != null ? List.copyOf(evidencePool) : Collections.emptyList();
    }

    @Override
    public String getName() {
        return "Evidence Forensic Analyzer";
    }

    @Override
    public AnalysisResult analyze(AnalysisContext context) throws Exception {
        long start = System.currentTimeMillis();
        List<String> findings = new ArrayList<>();
        Map<String, Object> metrics = new HashMap<>();

        int digitalCount = 0;
        int documentCount = 0;
        int unanalyzedCount = 0;
        List<String> unanalyzedNames = new ArrayList<>();

        for (Evidence ev : evidencePool) {
            if (!ev.isAnalyzed()) {
                unanalyzedCount++;
                unanalyzedNames.add(ev.getName());
            }

            if (ev instanceof DigitalEvidence) {
                digitalCount++;
                DigitalEvidence dig = (DigitalEvidence) ev;
                findings.add(String.format("Digital Forensic: %s [Device: %s, Hash: %s]",
                        dig.getName(), dig.getDeviceType(), dig.getFileHash()));
            } else if (ev instanceof DocumentEvidence) {
                documentCount++;
                DocumentEvidence doc = (DocumentEvidence) ev;
                findings.add(String.format("Document Forensic: %s [Author: %s, Type: %s, Confidential: %s]",
                        doc.getName(), doc.getAuthor(), doc.getDocumentType(), doc.isClassified()));
            } else {
                findings.add("General Asset: " + ev.getName() + " - " + ev.getSummary());
            }
        }

        metrics.put("totalEvidence", evidencePool.size());
        metrics.put("digitalEvidenceCount", digitalCount);
        metrics.put("documentEvidenceCount", documentCount);
        metrics.put("unanalyzedCount", unanalyzedCount);

        if (unanalyzedCount > 0) {
            findings.add(String.format("Forensic Advisory: %d item(s) pending lab analysis: %s",
                    unanalyzedCount, String.join(", ", unanalyzedNames)));
        }

        long duration = System.currentTimeMillis() - start;
        String summary = String.format("Analyzed %d evidence pieces (%d digital, %d document). %d pending lab work.",
                evidencePool.size(), digitalCount, documentCount, unanalyzedCount);

        return AnalysisResult.success(getName(), summary, findings, metrics, duration);
    }
}
