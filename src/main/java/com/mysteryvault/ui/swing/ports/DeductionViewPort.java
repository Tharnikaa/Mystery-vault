package com.mysteryvault.ui.swing.ports;

import com.mysteryvault.service.dto.DeductionRequest;
import com.mysteryvault.service.dto.DeductionResult;

/**
 * Presentation port for Case Solver and Final Deduction evaluation.
 * Owned by Member 4 (Module 11).
 */
public interface DeductionViewPort {
    DeductionResult submitDeduction(DeductionRequest request);
    DeductionResult getLastResult();
}
