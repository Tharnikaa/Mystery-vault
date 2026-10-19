package com.mysteryvault.repository;

import com.mysteryvault.model.Evidence;

import java.util.List;

/**
 * Repository interface for Evidence entities (Module 3).
 * Supports polymorphic Document and Digital evidence and distinguishes
 * between canonical case evidence and discovered investigation progress.
 */
public interface EvidenceRepository extends GenericRepository<Evidence, String> {

    /**
     * Lists all canonical evidence associated with a case.
     *
     * @param caseId Case ID
     * @return List of all evidence in the case catalog
     */
    List<Evidence> findByCaseId(String caseId);

    /**
     * Lists only the evidence items that have been discovered during active investigation.
     *
     * @param caseId Case ID
     * @return List of discovered evidence items
     */
    List<Evidence> findDiscoveredByCaseId(String caseId);

    /**
     * Updates the discovered status of an evidence item in an active investigation.
     *
     * @param evidenceId Evidence ID
     * @param discovered Discovery flag
     */
    void markDiscovered(String evidenceId, boolean discovered);

    /**
     * Links an evidence item to a case.
     *
     * @param caseId Case ID
     * @param evidenceId Evidence ID
     */
    void linkEvidenceToCase(String caseId, String evidenceId);
}
