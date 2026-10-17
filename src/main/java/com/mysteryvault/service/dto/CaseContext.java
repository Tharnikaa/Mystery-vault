package com.mysteryvault.service.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Module 11 context passed to CaseSolverEngine containing current investigation state.
 */
public class CaseContext implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String caseId;
    private final String caseTitle;
    private final Set<String> discoveredEvidenceIds;
    private final Set<String> discoveredClueIds;
    private final Set<String> uncoveredContradictionIds;

    public CaseContext(String caseId, String caseTitle, Set<String> discoveredEvidenceIds, Set<String> discoveredClueIds, Set<String> uncoveredContradictionIds) {
        this.caseId = caseId != null ? caseId : "CASE_103";
        this.caseTitle = caseTitle != null ? caseTitle : "The Poisoned Drink";
        this.discoveredEvidenceIds = discoveredEvidenceIds != null ? Collections.unmodifiableSet(new HashSet<>(discoveredEvidenceIds)) : Collections.emptySet();
        this.discoveredClueIds = discoveredClueIds != null ? Collections.unmodifiableSet(new HashSet<>(discoveredClueIds)) : Collections.emptySet();
        this.uncoveredContradictionIds = uncoveredContradictionIds != null ? Collections.unmodifiableSet(new HashSet<>(uncoveredContradictionIds)) : Collections.emptySet();
    }

    public String getCaseId() {
        return caseId;
    }

    public String getCaseTitle() {
        return caseTitle;
    }

    public Set<String> getDiscoveredEvidenceIds() {
        return discoveredEvidenceIds;
    }

    public Set<String> getDiscoveredClueIds() {
        return discoveredClueIds;
    }

    public Set<String> getUncoveredContradictionIds() {
        return uncoveredContradictionIds;
    }
}
