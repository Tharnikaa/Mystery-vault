package com.mysteryvault.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Module 3 — Clue Unlock Engine.
 * Manages deterministic clue prerequisites and validation against discovered evidence.
 */
public class ClueUnlockEngine {

    public boolean canUnlock(String prerequisiteEvidenceIds, Set<String> discoveredEvidenceIds) {
        if (prerequisiteEvidenceIds == null || prerequisiteEvidenceIds.trim().isEmpty()) {
            return true;
        }
        String[] reqs = prerequisiteEvidenceIds.split(",");
        for (String req : reqs) {
            String trimmed = req.trim();
            if (!trimmed.isEmpty() && !discoveredEvidenceIds.contains(trimmed)) {
                return false;
            }
        }
        return true;
    }

    public boolean unlockClueInDatabase(Connection conn, String clueId) throws SQLException {
        // 1. Fetch discovered evidence IDs
        Set<String> discovered = new HashSet<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT evidence_id FROM evidence WHERE is_discovered = 1");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                discovered.add(rs.getString("evidence_id"));
            }
        }

        // 2. Fetch clue prereqs
        String prereqs = null;
        try (PreparedStatement ps = conn.prepareStatement("SELECT prerequisite_evidence_ids FROM clues WHERE clue_id = ?")) {
            ps.setString(1, clueId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    prereqs = rs.getString("prerequisite_evidence_ids");
                } else {
                    return false; // Clue not found
                }
            }
        }

        // 3. Check prerequisites
        if (canUnlock(prereqs, discovered)) {
            try (PreparedStatement ps = conn.prepareStatement("UPDATE clues SET status = 'UNLOCKED' WHERE clue_id = ?")) {
                ps.setString(1, clueId);
                return ps.executeUpdate() > 0;
            }
        }
        return false;
    }
}
