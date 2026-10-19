package com.mysteryvault.ui.swing.adapters;

import com.mysteryvault.model.Suspect;
import com.mysteryvault.service.SampleDataFactory;
import com.mysteryvault.ui.swing.ports.SuspectViewPort;
import com.mysteryvault.ui.swing.viewmodel.SuspectViewModel;

import java.util.*;

/**
 * Production presentation adapter connecting SuspectViewPort to Member 1's Suspect models (Module 2).
 */
public class IntegratedSuspectAdapter implements SuspectViewPort {

    private final Map<String, SuspectViewModel> suspectRegistry = new LinkedHashMap<>();
    private final List<SuspectViewModel> orderedSuspects = new ArrayList<>();

    public IntegratedSuspectAdapter() {
        this(SampleDataFactory.createSampleSuspects());
    }

    public IntegratedSuspectAdapter(List<Suspect> domainSuspects) {
        initSuspects(domainSuspects);
    }

    private void initSuspects(List<Suspect> domainSuspects) {
        // Map canonical Member 1 suspects to high-fidelity view models with portrait assets
        for (Suspect s : domainSuspects) {
            String img;
            String alias;
            int trust;
            boolean defaultExpanded = false;

            if (s.getId().endsWith("-1") || s.getName().contains("Marcus") || s.getName().contains("Arjun")) {
                img = "suspect_arjun.jpg";
                alias = "ARJUN";
                trust = 45;
            } else if (s.getId().endsWith("-2") || s.getName().contains("Elena") || s.getName().contains("Meera")) {
                img = "suspect_meera.png";
                alias = "MEERA";
                trust = 35;
                defaultExpanded = true;
            } else {
                img = "suspect_rohan.png";
                alias = "ROHAN";
                trust = 65;
            }

            SuspectViewModel vm = new SuspectViewModel(
                    s.getId(),
                    s.getName().toUpperCase(),
                    "• " + s.getOccupation() + " •",
                    s.getAge(),
                    "Person of Interest",
                    s.getAlibi() != null ? s.getAlibi() : "Unspecified location",
                    s.getMotiveDescription() != null ? s.getMotiveDescription() : "Unknown motive",
                    s.getSuspicionScore(),
                    trust,
                    img,
                    defaultExpanded
            );

            orderedSuspects.add(vm);
            suspectRegistry.put(s.getId().toUpperCase(), vm);
            suspectRegistry.put(s.getName().toUpperCase(), vm);
            suspectRegistry.put(alias, vm);
        }

        // Ensure fallback aliases for UI buttons (e.g. ARJUN, MEERA, ROHAN)
        if (!suspectRegistry.containsKey("MEERA") && !orderedSuspects.isEmpty()) {
            suspectRegistry.put("MEERA", orderedSuspects.get(Math.min(1, orderedSuspects.size() - 1)));
        }
        if (!suspectRegistry.containsKey("ARJUN") && !orderedSuspects.isEmpty()) {
            suspectRegistry.put("ARJUN", orderedSuspects.get(0));
        }
        if (!suspectRegistry.containsKey("ROHAN") && orderedSuspects.size() > 2) {
            suspectRegistry.put("ROHAN", orderedSuspects.get(2));
        }
    }

    @Override
    public List<SuspectViewModel> getSuspectsForCase(String caseId) {
        return Collections.unmodifiableList(orderedSuspects);
    }

    @Override
    public SuspectViewModel getSuspectById(String suspectId) {
        if (suspectId == null || suspectId.trim().isEmpty()) {
            return orderedSuspects.isEmpty() ? null : orderedSuspects.get(0);
        }
        String key = suspectId.trim().toUpperCase();
        SuspectViewModel vm = suspectRegistry.get(key);
        if (vm != null) return vm;

        for (SuspectViewModel s : orderedSuspects) {
            if (s.getId().equalsIgnoreCase(key) || s.getName().equalsIgnoreCase(key)) {
                return s;
            }
        }
        return orderedSuspects.isEmpty() ? null : orderedSuspects.get(0);
    }
}
