package com.mysteryvault.ui.swing.adapters;

import com.mysteryvault.database.CaseDatabaseManager;
import com.mysteryvault.model.DetectiveRank;
import com.mysteryvault.service.CaseCompletionService;
import com.mysteryvault.service.CaseSolverEngine;
import com.mysteryvault.service.dto.CaseContext;
import com.mysteryvault.service.dto.DeductionRequest;
import com.mysteryvault.service.dto.DeductionResult;
import com.mysteryvault.ui.swing.ports.DeductionViewPort;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;

/**
 * Production implementation of DeductionViewPort evaluating accusations against canonical rules.
 */
public class IntegratedDeductionAdapter implements DeductionViewPort {

    private final CaseDatabaseManager dbManager;
    private final CaseSolverEngine solverEngine;
    private final CaseCompletionService completionService;
    private DeductionResult lastResult;

    public IntegratedDeductionAdapter(CaseDatabaseManager dbManager, CaseSolverEngine solverEngine) {
        this.dbManager = dbManager;
        this.solverEngine = solverEngine != null ? solverEngine : new CaseSolverEngine(false);
        this.completionService = new CaseCompletionService(dbManager);
        registerAllRules();
    }

    private void registerAllRules() {
        // Case 101: The Missing Diamond
        solverEngine.registerCaseRule(new CaseSolverEngine.CaseSolutionRule(
                "CASE_101",
                "SUS_101_3", // Marcus Vance
                "Financial desperation to pay off loan shark syndicates using a black-market diamond broker",
                new HashSet<>(Arrays.asList("EVD_101_SEC_LOG", "EVD_101_GLASS_CUTTER")),
                "Server RFID logs and biometric security records establish Marcus Vance accessed the vault during the blackout and cut the display with an engraved cutter.",
                "The presented evidence does not substantiate the accusation. RFID records and physical cutter analysis must be corroborated.",
                5, 3, 2, 95
        ));

        // Case 102: The Library Code
        solverEngine.registerCaseRule(new CaseSolverEngine.CaseSolutionRule(
                "CASE_102",
                "SUS_102_1", // Dr. Harrison Vance
                "Academic fraud concealment: Marginal notes in the cipher proved Vance plagiarized his magnum opus",
                new HashSet<>(Arrays.asList("EVD_102_SPECTACLE_FIBER", "EVD_102_BURNED_PAGE")),
                "Attendance logs confirm Vance dismissed class early. Distinctive green tweed fibers on the safe dial and scorched marginalia in his fireplace establish culpability.",
                "The presented evidence does not substantiate the accusation. Class attendance logs and physical fibers must be connected.",
                4, 3, 2, 90
        ));

        // Case 103: The Poisoned Drink
        solverEngine.registerCaseRule(new CaseSolverEngine.CaseSolutionRule(
                "CASE_103",
                "SUS_103_3", // Vivienne Sinclair
                "Preventing exposure of £1.2M company embezzlement which Lord Croft was scheduled to audit the next morning",
                new HashSet<>(Arrays.asList("EVD_103_COCKTAIL_GLASS", "EVD_103_POWDER_COMPACT")),
                "Forensic toxin analysis on Lord Croft's personalized crystal rim matches chemical powder found in Vivienne Sinclair's silver compact, proving premeditated poisoning.",
                "The presented evidence does not substantiate the accusation. Chemical residue on the rim must match the compact source.",
                4, 3, 2, 92
        ));

        // Case 104: The Vanishing Prototype
        solverEngine.registerCaseRule(new CaseSolverEngine.CaseSolutionRule(
                "CASE_104",
                "SUS_104_2", // Kira Patel
                "Corporate espionage: Selling proprietary core telemetry and physical prototype to rival syndicate TitanCorp",
                new HashSet<>(Arrays.asList("EVD_104_FARADAY_BAG", "EVD_104_FLASH_DRIVE")),
                "Airlock biometric scans disprove Kira's cafeteria alibi. The RF-shielded Faraday bag bearing her asset serial and the encrypted telemetry drive in her car establish industrial espionage.",
                "The presented evidence does not substantiate the accusation. Airlock entry records and the Faraday pouch must be connected.",
                4, 3, 2, 96
        ));
    }

    @Override
    public DeductionResult submitDeduction(DeductionRequest request) {
        String caseId = CaseDatabaseManager.normalizeCaseId(request.getCaseId());
        CaseContext context = buildCaseContext(caseId);

        // Normalize accused suspect ID if needed (e.g. if player selected "Marcus Vance" instead of "SUS_101_3")
        String accusedId = request.getAccusedSuspectId();
        accusedId = resolveSuspectId(caseId, accusedId);

        // Normalize motive if needed
        String motive = resolveMotive(caseId, request.getMotive());

        DeductionRequest normalizedReq = new DeductionRequest(
                caseId,
                accusedId,
                motive,
                request.getSupportingEvidenceIds(),
                request.getReasoning()
        );

        DeductionResult result = solverEngine.evaluate(normalizedReq, context);

        // Persist attempt in DB
        persistAttempt(caseId, normalizedReq, result);

        // If solved, atomically and idempotently update case, catalog & player profile
        if (result.isSolved()) {
            CaseCompletionService.CompletionOutcome outcome = completionService.completeCaseIfSolved(caseId, result);
            if (!outcome.isSuccess()) {
                result = new DeductionResult(
                        false,
                        result.getScore(),
                        "DATABASE ERROR",
                        result.getCaseTitle(),
                        "Completion could not be recorded: " + outcome.getErrorMessage(),
                        "IN_PROGRESS",
                        result.getEvidenceFoundCount(),
                        result.getTotalEvidenceCount(),
                        result.getImportantCluesCount(),
                        result.getTotalCluesCount(),
                        result.getContradictionsFound(),
                        result.getTotalContradictions(),
                        result.getWrongAccusations(),
                        DetectiveRank.getRankTitle(0)
                );
            }
        } else {
            completionService.recordAttempt(caseId, false, result.getScore());
        }

        this.lastResult = result;
        return result;
    }

    @Override
    public DeductionResult getLastResult() {
        if (lastResult != null) {
            return lastResult;
        }
        return new DeductionResult(
                true,
                92,
                "CASE SOLVED",
                "CASE #103 – The Poisoned Drink",
                "Forensic toxin analysis proves the suspect had direct access to the glass during the critical timeline window.",
                "SOLVED",
                8, 8,
                6, 6,
                3, 3,
                0,
                "MASTER DETECTIVE"
        );
    }

    private CaseContext buildCaseContext(String caseId) {
        Set<String> discoveredEv = new HashSet<>();
        Set<String> discoveredClues = new HashSet<>();
        Set<String> contradictions = new HashSet<>();
        String title = "CASE #" + caseId;

        try (Connection conn = dbManager.getCaseConnection(caseId)) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT title FROM case_metadata");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    title = rs.getString("title");
                }
            }
            try (PreparedStatement ps = conn.prepareStatement("SELECT evidence_id FROM evidence WHERE is_discovered = 1");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    discoveredEv.add(rs.getString("evidence_id"));
                }
            }
            try (PreparedStatement ps = conn.prepareStatement("SELECT clue_id FROM clues WHERE status = 'UNLOCKED'");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    discoveredClues.add(rs.getString("clue_id"));
                }
            }
            try (PreparedStatement ps = conn.prepareStatement("SELECT contradiction_id FROM contradictions");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    contradictions.add(rs.getString("contradiction_id"));
                }
            }
        } catch (SQLException ignored) {
        }

        return new CaseContext(caseId, title, discoveredEv, discoveredClues, contradictions);
    }

    private String resolveSuspectId(String caseId, String rawSuspect) {
        if (rawSuspect == null) return "UNKNOWN";
        try (Connection conn = dbManager.getCaseConnection(caseId);
             PreparedStatement ps = conn.prepareStatement("SELECT suspect_id FROM suspects WHERE suspect_id = ? OR name LIKE ?")) {
            ps.setString(1, rawSuspect.trim());
            ps.setString(2, "%" + rawSuspect.trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("suspect_id");
                }
            }
        } catch (SQLException ignored) {
        }
        return rawSuspect.trim();
    }

    private String resolveMotive(String caseId, String rawMotive) {
        CaseSolverEngine.CaseSolutionRule rule = solverEngine.getCaseRule(caseId);
        if (rule != null && rawMotive != null) {
            String exp = rule.getExpectedMotive().toLowerCase();
            String raw = rawMotive.toLowerCase();
            if (exp.contains(raw) || raw.contains(exp) || raw.contains("desperation") || raw.contains("embezzlement") || raw.contains("espionage") || raw.contains("plagiar")) {
                return rule.getExpectedMotive();
            }
        }
        return rawMotive != null ? rawMotive.trim() : "";
    }

    private void persistAttempt(String caseId, DeductionRequest req, DeductionResult result) {
        String attemptId = "ATTEMPT_" + System.currentTimeMillis();
        String evCsv = String.join(",", req.getSupportingEvidenceIds());
        try (Connection conn = dbManager.getCaseConnection(caseId);
             PreparedStatement ps = conn.prepareStatement("INSERT INTO deduction_attempts (attempt_id, suspect_id, motive, evidence_ids, is_correct, score, feedback, attempted_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, attemptId);
            ps.setString(2, req.getAccusedSuspectId());
            ps.setString(3, req.getMotive());
            ps.setString(4, evCsv);
            ps.setInt(5, result.isSolved() ? 1 : 0);
            ps.setInt(6, result.getScore());
            ps.setString(7, result.getExplanation());
            ps.setString(8, Instant.now().toString());
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    public CaseCompletionService getCompletionService() {
        return completionService;
    }
}
