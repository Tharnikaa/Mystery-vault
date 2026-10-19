package com.mysteryvault.ui.swing.adapters;

import com.mysteryvault.model.*;
import com.mysteryvault.service.ClueUnlockEngine;
import com.mysteryvault.service.SampleDataFactory;
import com.mysteryvault.ui.swing.ports.EvidenceViewPort;
import com.mysteryvault.ui.swing.viewmodel.EvidenceViewModel;

import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Production presentation adapter connecting EvidenceViewPort to Member 1's Evidence, Clue,
 * and ClueUnlockEngine services (Module 3).
 */
public class IntegratedEvidenceAdapter implements EvidenceViewPort {

    private final ClueUnlockEngine clueUnlockEngine;
    private final List<Evidence> domainEvidenceList;
    private final List<Clue> domainClues;
    private final Set<String> discoveredEvidenceIds = new HashSet<>();
    private final Map<String, EvidenceViewModel> viewModelMap = new LinkedHashMap<>();
    private final List<EvidenceViewModel> orderedViewModels = new ArrayList<>();

    public IntegratedEvidenceAdapter() {
        this(new ClueUnlockEngine(), SampleDataFactory.createSampleEvidence(), SampleDataFactory.createSampleClues());
    }

    public IntegratedEvidenceAdapter(ClueUnlockEngine clueUnlockEngine,
                                   List<Evidence> evidenceList,
                                   List<Clue> clues) {
        this.clueUnlockEngine = Objects.requireNonNull(clueUnlockEngine, "clueUnlockEngine cannot be null");
        this.domainEvidenceList = new ArrayList<>(evidenceList != null ? evidenceList : Collections.emptyList());
        this.domainClues = new ArrayList<>(clues != null ? clues : Collections.emptyList());

        initViewModels();
    }

    private void initViewModels() {
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("hh:mm a");

        for (int i = 0; i < domainEvidenceList.size(); i++) {
            Evidence ev = domainEvidenceList.get(i);
            String shortCode = "E0" + (i + 1);
            String timeStr = ev.getDiscoveredAt() != null ? ev.getDiscoveredAt().format(timeFmt) : "08:30 PM";

            String typeStr;
            String imgKey;
            if (ev instanceof DigitalEvidence) {
                typeStr = "Digital Forensics";
                imgKey = "case_prototype.png";
            } else if (ev instanceof DocumentEvidence) {
                typeStr = "Documentary Evidence";
                imgKey = i == 0 ? "evidence_wineglass.png" : "case_diamond.png";
            } else {
                typeStr = "Physical Evidence";
                imgKey = "evidence_wineglass.png";
            }

            EvidenceViewModel vm = new EvidenceViewModel(
                    ev.getId(),
                    shortCode + " — " + ev.getName(),
                    typeStr,
                    ev.getLocationFound() != null ? ev.getLocationFound() : "Crime Scene",
                    timeStr,
                    ev.getDescription(),
                    imgKey
            );

            orderedViewModels.add(vm);
            viewModelMap.put(ev.getId().toUpperCase(), vm);
            viewModelMap.put(shortCode.toUpperCase(), vm);
        }

        // Add additional sample items if needed to match full UI list
        if (orderedViewModels.size() < 5) {
            EvidenceViewModel e4 = new EvidenceViewModel(
                    "EVD-103-4", "E04 — Encrypted Phone Log", "Telecommunications", "Victim's Smartphone", "09:10 PM",
                    "Incoming encrypted call lasting 47 seconds from an untraceable burner telephone.", "case_library.png"
            );
            orderedViewModels.add(e4);
            viewModelMap.put(e4.getId().toUpperCase(), e4);
            viewModelMap.put("E04", e4);

            EvidenceViewModel e5 = new EvidenceViewModel(
                    "EVD-103-5", "E05 — Toxicology Screen", "Forensic Lab", "County Morgue", "10:30 PM",
                    "Toxicology confirms rapid-acting neurotoxin matching laboratory-grade synthetic alkaloid.", "case_poison.png"
            );
            orderedViewModels.add(e5);
            viewModelMap.put(e5.getId().toUpperCase(), e5);
            viewModelMap.put("E05", e5);
        }
    }

    @Override
    public List<EvidenceViewModel> getEvidenceForCase(String caseId) {
        return Collections.unmodifiableList(orderedViewModels);
    }

    @Override
    public EvidenceViewModel getEvidenceById(String evidenceId) {
        if (evidenceId == null || evidenceId.trim().isEmpty()) {
            return orderedViewModels.isEmpty() ? null : orderedViewModels.get(0);
        }
        String key = evidenceId.trim().toUpperCase();
        EvidenceViewModel vm = viewModelMap.get(key);
        if (vm != null) return vm;

        for (EvidenceViewModel e : orderedViewModels) {
            if (e.getId().equalsIgnoreCase(key) || e.getTitle().toUpperCase().contains(key)) {
                return e;
            }
        }
        return orderedViewModels.isEmpty() ? null : orderedViewModels.get(0);
    }

    @Override
    public String examineEvidence(String evidenceId) {
        discoveredEvidenceIds.add(evidenceId);
        EvidenceViewModel vm = getEvidenceById(evidenceId);
        String name = vm != null ? vm.getTitle() : evidenceId;

        // Check for clue unlocking via ClueUnlockEngine
        StringBuilder sb = new StringBuilder();
        sb.append("Examined: ").append(name).append("\n");
        sb.append("Chain of Custody verified. Fingerprint smudges and chemical residue documented.\n\n");

        List<String> unlockedNow = new ArrayList<>();
        for (Clue clue : domainClues) {
            if (!clue.isUnlocked() && clueUnlockEngine.canUnlock(clue, discoveredEvidenceIds)) {
                try {
                    clueUnlockEngine.unlock(clue, discoveredEvidenceIds);
                    unlockedNow.add("'" + clue.getTitle() + "'");
                } catch (Exception ignored) {
                }
            }
        }

        if (!unlockedNow.isEmpty()) {
            sb.append(">>> NEW CLUE UNLOCKED! <<<\n");
            sb.append(String.join(", ", unlockedNow)).append("\n");
            sb.append("Added to investigation connection board.");
        } else {
            sb.append("Status: Evidence cataloged. Additional corroborating items required to unlock new leads.");
        }

        return sb.toString();
    }

    @Override
    public String sendToLab(String evidenceId) {
        discoveredEvidenceIds.add(evidenceId);
        Evidence found = null;
        for (Evidence ev : domainEvidenceList) {
            if (ev.getId().equalsIgnoreCase(evidenceId) || evidenceId.toUpperCase().contains(ev.getId().toUpperCase())) {
                found = ev;
                break;
            }
        }

        if (found != null) {
            found.setAnalyzed(true);
            return "LAB REPORT #L-" + evidenceId + "\n" +
                    "Specimen: " + found.getName() + "\n" +
                    "Status: Forensic analysis COMPLETE.\n" +
                    "Results: " + found.getSummary();
        }

        return "LAB REPORT #L-" + evidenceId + "\n" +
                "Status: High-resolution spectral analysis completed.\n" +
                "Findings: Traces match synthetic belladonna alkaloid compound.";
    }

    public Set<String> getDiscoveredEvidenceIds() {
        return Collections.unmodifiableSet(discoveredEvidenceIds);
    }

    public List<Clue> getDomainClues() {
        return Collections.unmodifiableList(domainClues);
    }
}
