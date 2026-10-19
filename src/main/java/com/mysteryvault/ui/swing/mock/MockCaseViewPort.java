package com.mysteryvault.ui.swing.mock;

import com.mysteryvault.ui.swing.ports.CaseViewPort;
import com.mysteryvault.ui.swing.viewmodel.CaseCardViewModel;

import java.util.ArrayList;
import java.util.List;

public class MockCaseViewPort implements CaseViewPort {

    private final List<CaseCardViewModel> cases = new ArrayList<>();
    private String activeCaseId = "CASE_103";

    public MockCaseViewPort() {
        cases.add(new CaseCardViewModel(
                "CASE_101", "CASE #101 – The Stolen Diamond", "30 Mins",
                "SOLVED", "SOLVED", "case_diamond.png", false
        ));
        cases.add(new CaseCardViewModel(
                "CASE_102", "CASE #102 – The Library Code", "40 Mins",
                "SOLVED", "SOLVED", "case_library.png", false
        ));
        cases.add(new CaseCardViewModel(
                "CASE_103", "CASE #103 – The Poisoned Drink", "45 Mins • 75% Complete",
                "IN PROGRESS", "IN_PROGRESS", "case_poison.png", true
        ));
        cases.add(new CaseCardViewModel(
                "CASE_104", "CASE #104 – The Missing Prototype", "50 Mins",
                "LOCKED", "LOCKED", "case_prototype.png", false
        ));
    }

    @Override
    public List<CaseCardViewModel> getAvailableCases() {
        return new ArrayList<>(cases);
    }

    @Override
    public CaseCardViewModel getActiveCase() {
        for (CaseCardViewModel c : cases) {
            if (c.getId().equals(activeCaseId)) return c;
        }
        return cases.get(2);
    }

    @Override
    public void selectCase(String caseId) {
        this.activeCaseId = caseId;
    }
}
