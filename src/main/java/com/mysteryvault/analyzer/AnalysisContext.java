package com.mysteryvault.analyzer;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Immutable context provided to analyzers for execution.
 * Shared DTO owned by Member 3 (Module 8: Concurrent Analysis).
 */
public class AnalysisContext implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String caseId;
    private final String caseTitle;
    private final List<String> suspectIds;
    private final List<String> evidenceIds;
    private final List<String> clueIds;
    private final Map<String, Object> parameters;

    public AnalysisContext(String caseId,
                           String caseTitle,
                           List<String> suspectIds,
                           List<String> evidenceIds,
                           List<String> clueIds,
                           Map<String, Object> parameters) {
        this.caseId = caseId != null ? caseId : "";
        this.caseTitle = caseTitle != null ? caseTitle : "";
        this.suspectIds = suspectIds != null ? List.copyOf(suspectIds) : Collections.emptyList();
        this.evidenceIds = evidenceIds != null ? List.copyOf(evidenceIds) : Collections.emptyList();
        this.clueIds = clueIds != null ? List.copyOf(clueIds) : Collections.emptyList();
        this.parameters = parameters != null ? Map.copyOf(parameters) : Collections.emptyMap();
    }

    public String getCaseId() {
        return caseId;
    }

    public String getCaseTitle() {
        return caseTitle;
    }

    public List<String> getSuspectIds() {
        return suspectIds;
    }

    public List<String> getEvidenceIds() {
        return evidenceIds;
    }

    public List<String> getClueIds() {
        return clueIds;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    @SuppressWarnings("unchecked")
    public <T> T getParameter(String key, Class<T> clazz) {
        Object val = parameters.get(key);
        if (val != null && clazz.isInstance(val)) {
            return (T) val;
        }
        return null;
    }
}
