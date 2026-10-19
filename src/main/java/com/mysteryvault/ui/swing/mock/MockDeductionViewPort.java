package com.mysteryvault.ui.swing.mock;

import com.mysteryvault.service.CaseSolverEngine;
import com.mysteryvault.service.dto.CaseContext;
import com.mysteryvault.service.dto.DeductionRequest;
import com.mysteryvault.service.dto.DeductionResult;
import com.mysteryvault.ui.swing.ports.DeductionViewPort;

import java.util.HashSet;
import java.util.Set;

public class MockDeductionViewPort implements DeductionViewPort {

    private final CaseSolverEngine solverEngine;
    private DeductionResult lastResult;

    public MockDeductionViewPort(CaseSolverEngine solverEngine) {
        this.solverEngine = solverEngine != null ? solverEngine : new CaseSolverEngine();
        // Default initial result matching reference screen 12
        this.lastResult = new DeductionResult(
                true, 92, "CASE SOLVED", "THE POISONED DRINK",
                "Forensic toxin analysis on the wine glass (E01) combined with CCTV footage (E03) proves Meera had sole access to the glass during the critical 8:45 PM timeline window.",
                "SOLVED",
                8, 8,
                6, 6,
                3, 3,
                0,
                "MASTER DETECTIVE"
        );
    }

    public MockDeductionViewPort() {
        this(new CaseSolverEngine());
    }

    @Override
    public DeductionResult submitDeduction(DeductionRequest request) {
        Set<String> discovered = new HashSet<>(java.util.Arrays.asList("E01", "E02", "E03", "E04", "E05", "E06", "E07", "E08"));
        Set<String> clues = new HashSet<>(java.util.Arrays.asList("C01", "C02", "C03", "C04", "C05", "C06"));
        Set<String> contradictions = new HashSet<>(java.util.Arrays.asList("K01", "K02", "K03"));

        CaseContext context = new CaseContext(
                request.getCaseId(),
                "THE POISONED DRINK",
                discovered,
                clues,
                contradictions
        );
        this.lastResult = solverEngine.evaluate(request, context);
        return this.lastResult;
    }

    @Override
    public DeductionResult getLastResult() {
        return lastResult;
    }
}
