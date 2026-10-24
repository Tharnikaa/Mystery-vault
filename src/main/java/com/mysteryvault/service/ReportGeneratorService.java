package com.mysteryvault.service;

import com.mysteryvault.database.CaseDatabaseManager;
import com.mysteryvault.ui.swing.viewmodel.ReportViewModel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Module 10 — Report Generator Service.
 * Compiles comprehensive official investigation dossiers and exports them to disk.
 */
public class ReportGeneratorService {

    private final CaseDatabaseManager dbManager;

    public ReportGeneratorService(CaseDatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public ReportViewModel generateReport(String caseId) {
        String normalized = CaseDatabaseManager.normalizeCaseId(caseId);
        String title = "CASE #" + normalized;
        String culprit = "Unknown";
        String motive = "Under Investigation";
        String status = "OPEN";
        StringBuilder summary = new StringBuilder();

        try (Connection conn = dbManager.getCaseConnection(normalized)) {
            // Case metadata
            try (PreparedStatement ps = conn.prepareStatement("SELECT title, summary, status FROM case_metadata");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    title = rs.getString("title");
                    status = rs.getString("status");
                    summary.append("CASE SYNOPSIS:\n").append(rs.getString("summary")).append("\n\n");
                }
            }

            // Solution info
            try (PreparedStatement ps = conn.prepareStatement("SELECT culprit_name, motive, explanation FROM canonical_solution");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    culprit = rs.getString("culprit_name");
                    motive = rs.getString("motive");
                    summary.append("OFFICIAL VERDICT & FINDINGS:\n").append(rs.getString("explanation")).append("\n\n");
                }
            }

            // Evidence summary
            summary.append("DISCOVERED EVIDENCE CATALOG:\n");
            try (PreparedStatement ps = conn.prepareStatement("SELECT name, location_found, significance FROM evidence WHERE is_discovered = 1");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    summary.append(" - ").append(rs.getString("name"))
                            .append(" [").append(rs.getString("significance")).append("]")
                            .append(" found at ").append(rs.getString("location_found")).append("\n");
                }
            }

        } catch (Exception e) {
            summary.append("Error compiling report from database: ").append(e.getMessage());
        }

        return new ReportViewModel(normalized, title, culprit, motive, status, summary.toString());
    }

    public boolean exportReportToFile(String caseId, File destinationFile) {
        ReportViewModel vm = generateReport(caseId);
        if (destinationFile.getParentFile() != null && !destinationFile.getParentFile().exists()) {
            destinationFile.getParentFile().mkdirs();
        }

        try (FileWriter writer = new FileWriter(destinationFile)) {
            writer.write("================================================================================\n");
            writer.write("                  MYSTERYVAULT OFFICIAL INVESTIGATION DOSSIER                   \n");
            writer.write("================================================================================\n");
            writer.write("CASE ID:    " + vm.getCaseId() + "\n");
            writer.write("CASE TITLE: " + vm.getCaseTitle() + "\n");
            writer.write("STATUS:     " + vm.getStatus() + "\n");
            writer.write("DATE:       " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
            writer.write("CULPRIT:    " + vm.getCulpritName() + "\n");
            writer.write("MOTIVE:     " + vm.getMotive() + "\n");
            writer.write("--------------------------------------------------------------------------------\n\n");
            writer.write(vm.getOfficialSummary());
            writer.write("\n================================================================================\n");
            writer.write("                  CONFIDENTIAL — BUREAU OF CRIMINAL INVESTIGATION               \n");
            writer.write("================================================================================\n");
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
