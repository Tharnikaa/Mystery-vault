package com.mysteryvault.repository;

import com.mysteryvault.model.Suspect;

import java.util.List;

/**
 * Repository interface for Suspect & Person entities (Module 2).
 */
public interface SuspectRepository extends GenericRepository<Suspect, String> {

    /**
     * Lists all suspects associated with a specific case.
     * Prevents suspects from leaking across case boundaries.
     *
     * @param caseId Case ID
     * @return List of suspects linked to the case
     */
    List<Suspect> findByCaseId(String caseId);

    /**
     * Searches for suspects whose names match the query.
     *
     * @param nameQuery Substring name search
     * @return List of matching suspects
     */
    List<Suspect> searchByName(String nameQuery);

    /**
     * Links an existing suspect to a case.
     *
     * @param caseId Case ID
     * @param suspectId Suspect ID
     */
    void linkSuspectToCase(String caseId, String suspectId);

    /**
     * Unlinks a suspect from a case without deleting the suspect profile.
     *
     * @param caseId Case ID
     * @param suspectId Suspect ID
     */
    void unlinkSuspectFromCase(String caseId, String suspectId);
}
