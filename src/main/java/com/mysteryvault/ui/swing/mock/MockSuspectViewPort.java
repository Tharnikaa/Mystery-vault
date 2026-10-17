package com.mysteryvault.ui.swing.mock;

import com.mysteryvault.ui.swing.ports.SuspectViewPort;
import com.mysteryvault.ui.swing.viewmodel.SuspectViewModel;

import java.util.ArrayList;
import java.util.List;

public class MockSuspectViewPort implements SuspectViewPort {

    private final List<SuspectViewModel> suspects = new ArrayList<>();

    public MockSuspectViewPort() {
        suspects.add(new SuspectViewModel(
                "ARJUN", "ARJUN", "— Businessman —", 35, "Single",
                "Bar Lounge", "Business Partner", 45, 60,
                "suspect_arjun.jpg", false
        ));
        suspects.add(new SuspectViewModel(
                "MEERA", "MEERA", "— Accountant —", 32, "In Relationship",
                "Restaurant VIP Table", "Financial Auditor", 72, 45,
                "suspect_meera.png", true
        ));
        suspects.add(new SuspectViewModel(
                "ROHAN", "ROHAN", "— Chef —", 29, "Married",
                "Restaurant Kitchen", "Head Chef", 31, 80,
                "suspect_rohan.png", false
        ));
    }

    @Override
    public List<SuspectViewModel> getSuspectsForCase(String caseId) {
        return new ArrayList<>(suspects);
    }

    @Override
    public SuspectViewModel getSuspectById(String suspectId) {
        for (SuspectViewModel s : suspects) {
            if (s.getId().equalsIgnoreCase(suspectId) || s.getName().equalsIgnoreCase(suspectId)) {
                return s;
            }
        }
        return suspects.get(1); // Default to Meera
    }
}
