package com.mysteryvault.service;

import com.mysteryvault.enums.CaseStatus;
import com.mysteryvault.model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Predictable mock sample data provider for Case #103 'The Poisoned Drink'.
 * Explicitly labeled as synthetic demonstration data for team integration.
 */
public final class SampleDataFactory {

    private SampleDataFactory() {
        // Utility class
    }

    /**
     * Creates sample Case #103 'The Poisoned Drink'.
     */
    public static Case createSampleCase() {
        Case sample = new Case(
                "CASE-103",
                "The Poisoned Drink",
                "Billionaire Julian Vance collapsed during a private gallery gala after drinking poisoned vintage wine.",
                CaseStatus.ACTIVE,
                Priority.HIGH,
                "Detective Adrian Miller",
                LocalDateTime.of(2026, 10, 15, 20, 30),
                LocalDateTime.of(2026, 10, 15, 21, 0),
                null, null, null
        );

        sample.addSuspectId("SUS-103-1");
        sample.addSuspectId("SUS-103-2");
        sample.addSuspectId("SUS-103-3");

        sample.addEvidenceId("EVD-103-1");
        sample.addEvidenceId("EVD-103-2");
        sample.addEvidenceId("EVD-103-3");

        sample.addClueId("CLU-103-1");
        sample.addClueId("CLU-103-2");

        return sample;
    }

    /**
     * Creates the list of 3 suspects associated with Case #103.
     */
    public static List<Suspect> createSampleSuspects() {
        List<Suspect> suspects = new ArrayList<>();

        // Suspect 1
        Suspect marcus = new Suspect(
                "SUS-103-1",
                "Marcus Vance",
                "Heir & Gallery Director",
                34,
                "Claimed he was checking security feeds in the back office.",
                78,
                "Recently discovered amendment in Julian's will reducing his inheritance.",
                false
        );
        marcus.setNotes("Agitated during preliminary interview; has substantial gambling debts.");
        suspects.add(marcus);

        // Suspect 2
        Suspect elena = new Suspect(
                "SUS-103-2",
                "Elena Rostova",
                "Lead Sommelier",
                29,
                "Serving guests in the east salon; wine bottle was unattended for 4 minutes.",
                65,
                "Fired from previous Michelin restaurant following an unexplained cellar shortage.",
                true
        );
        elena.setNotes("Cooperative but visibly frightened; fingerprints found on decanter neck.");
        suspects.add(elena);

        // Suspect 3
        Suspect price = new Suspect(
                "SUS-103-3",
                "Dr. Warren Price",
                "Personal Physician",
                58,
                "Arrived late after visiting a patient at St. Jude's hospital.",
                42,
                "Refused medical malpractice audit requested by Julian Vance last month.",
                true
        );
        price.setNotes("Possesses medical prescription access to rare pharmaceutical alkaloids.");
        suspects.add(price);

        return Collections.unmodifiableList(suspects);
    }

    /**
     * Creates sample evidence items for Case #103 (polymorphic Digital and Document evidence).
     */
    public static List<Evidence> createSampleEvidence() {
        List<Evidence> evidenceList = new ArrayList<>();

        // Document Evidence 1
        DocumentEvidence toxReport = new DocumentEvidence(
                "EVD-103-1",
                "Autopsy Toxicology Screen",
                "CASE-103",
                "Traces of rare belladonna alkaloid found in victim's bloodstream and wine glass residue.",
                LocalDateTime.of(2026, 10, 16, 8, 15),
                "County Morgue Pathology Lab",
                "Chief Medical Examiner Dr. L. Chen",
                true,
                "County Coroner Office",
                4,
                true,
                "Toxicology Report"
        );
        evidenceList.add(toxReport);

        // Digital Evidence 1
        DigitalEvidence cameraFeed = new DigitalEvidence(
                "EVD-103-2",
                "Gala Security Camera Feed #04",
                "CASE-103",
                "Timestamp 20:42 shows a gloved figure entering the sommelier cellar pantry for 48 seconds.",
                LocalDateTime.of(2026, 10, 15, 22, 10),
                "Gallery Security Control Room Server Rack",
                "Cyber Forensics Officer K. Tanaka",
                true,
                "Hikvision NVR Server",
                "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                "192.168.10.24",
                2_450_000_000L
        );
        evidenceList.add(cameraFeed);

        // Document Evidence 2
        DocumentEvidence willDoc = new DocumentEvidence(
                "EVD-103-3",
                "Vance Family Estate Will & Testament",
                "CASE-103",
                "Recent codicil amendment disinherits Marcus Vance in favor of the City Arts Foundation.",
                LocalDateTime.of(2026, 10, 16, 11, 0),
                "Julian Vance Private Study Safe",
                "Detective Adrian Miller",
                false,
                "Sterling & Croft LLP",
                12,
                false,
                "Legal Contract"
        );
        evidenceList.add(willDoc);

        return Collections.unmodifiableList(evidenceList);
    }

    /**
     * Creates sample clues for Case #103 with evidence prerequisites.
     */
    public static List<Clue> createSampleClues() {
        List<Clue> clues = new ArrayList<>();

        // Clue 1 requires Camera Feed (EVD-103-2)
        Clue clue1 = new Clue(
                "CLU-103-1",
                "CASE-103",
                "Pantry Intruder Identification",
                "Video frame enhancement reveals a signet ring matching the Vance family crest on the gloved hand.",
                Set.of("EVD-103-2")
        );
        clues.add(clue1);

        // Clue 2 requires Toxicology (EVD-103-1) AND Will Amendment (EVD-103-3)
        Clue clue2 = new Clue(
                "CLU-103-2",
                "CASE-103",
                "Lethal Motive & Toxin Connection",
                "The disinheritance codicil was executed 48 hours prior; Marcus placed an overseas order for botanical belladonna extract.",
                Set.of("EVD-103-1", "EVD-103-3")
        );
        clues.add(clue2);

        return Collections.unmodifiableList(clues);
    }
}
