package com.mysteryvault.ui.swing.adapters;

import com.mysteryvault.enums.CaseStatus;
import com.mysteryvault.model.Case;
import com.mysteryvault.service.CaseService;
import com.mysteryvault.ui.swing.ports.CaseViewPort;
import com.mysteryvault.ui.swing.viewmodel.CaseCardViewModel;

import java.util.*;

/**
 * Production presentation adapter connecting CaseViewPort to Member 1's CaseService (Module 1).
 */
public class IntegratedCaseAdapter implements CaseViewPort {

    private final CaseService caseService;
    private String activeCaseId = "CASE-103";

    public IntegratedCaseAdapter(CaseService caseService) {
        this.caseService = Objects.requireNonNull(caseService, "caseService cannot be null");
        ensureDefaultCasesLoaded();
    }

    private void ensureDefaultCasesLoaded() {
        if (caseService.listCases().isEmpty()) {
            caseService.loadSampleCase103();
        }
    }

    @Override
    public List<CaseCardViewModel> getAvailableCases() {
        List<Case> domainCases = caseService.listCases();
        List<CaseCardViewModel> viewModels = new ArrayList<>();

        for (Case c : domainCases) {
            String badgeText;
            String badgeType;
            if (c.getStatus() == CaseStatus.SOLVED || c.getStatus() == CaseStatus.ARCHIVED) {
                badgeText = "SOLVED";
                badgeType = "SOLVED";
            } else if (c.getStatus() == CaseStatus.ACTIVE || c.getStatus() == CaseStatus.OPEN) {
                badgeText = "IN PROGRESS";
                badgeType = "IN_PROGRESS";
            } else {
                badgeText = "COLD CASE";
                badgeType = "LOCKED";
            }

            String imgKey = resolveImageKey(c.getId());
            String subtitle = (c.getPriority() != null ? c.getPriority().name() : "MEDIUM") + " PRIORITY • " + c.getStatus();
            boolean isSelected = c.getId().equalsIgnoreCase(activeCaseId) ||
                    c.getId().replace("-", "_").equalsIgnoreCase(activeCaseId);

            viewModels.add(new CaseCardViewModel(
                    c.getId(),
                    c.getTitle(),
                    subtitle,
                    badgeText,
                    badgeType,
                    imgKey,
                    isSelected
            ));
        }

        // Ensure at least Case 103 and demo gallery items are present for rich UI display
        if (viewModels.size() == 1) {
            viewModels.add(0, new CaseCardViewModel(
                    "CASE-101", "CASE #101 – The Stolen Diamond", "HIGH PRIORITY • SOLVED",
                    "SOLVED", "SOLVED", "case_diamond.png", false
            ));
            viewModels.add(1, new CaseCardViewModel(
                    "CASE-102", "CASE #102 – The Library Code", "MEDIUM PRIORITY • SOLVED",
                    "SOLVED", "SOLVED", "case_library.png", false
            ));
            viewModels.add(new CaseCardViewModel(
                    "CASE-104", "CASE #104 – The Missing Prototype", "CRITICAL PRIORITY • LOCKED",
                    "LOCKED", "LOCKED", "case_prototype.png", false
            ));
        }

        return Collections.unmodifiableList(viewModels);
    }

    @Override
    public CaseCardViewModel getActiveCase() {
        List<CaseCardViewModel> available = getAvailableCases();
        for (CaseCardViewModel vm : available) {
            if (vm.getId().equalsIgnoreCase(activeCaseId) ||
                    vm.getId().replace("-", "_").equalsIgnoreCase(activeCaseId) ||
                    activeCaseId.replace("-", "_").equalsIgnoreCase(vm.getId().replace("-", "_"))) {
                return vm;
            }
        }
        return available.isEmpty() ? null : available.get(0);
    }

    @Override
    public void selectCase(String caseId) {
        if (caseId != null && !caseId.trim().isEmpty()) {
            this.activeCaseId = caseId.trim();
        }
    }

    public CaseService getCaseService() {
        return caseService;
    }

    public String getActiveCaseId() {
        return activeCaseId;
    }

    private String resolveImageKey(String caseId) {
        if (caseId == null) return "case_poison.png";
        String lower = caseId.toLowerCase();
        if (lower.contains("101") || lower.contains("diamond")) return "case_diamond.png";
        if (lower.contains("102") || lower.contains("library")) return "case_library.png";
        if (lower.contains("104") || lower.contains("prototype")) return "case_prototype.png";
        return "case_poison.png";
    }
}
