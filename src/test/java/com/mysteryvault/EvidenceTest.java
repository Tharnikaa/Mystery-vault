package com.mysteryvault;

import com.mysteryvault.model.DigitalEvidence;
import com.mysteryvault.model.DocumentEvidence;
import com.mysteryvault.model.Evidence;
import com.mysteryvault.model.InvestigationAsset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EvidenceTest {

    @Test
    @DisplayName("Document evidence polymorphism and getSummary()")
    void testDocumentEvidence() {
        DocumentEvidence doc = new DocumentEvidence(
                "EVD-DOC", "Lab Analysis", "CASE-01", "Chemical traces of cyanide",
                LocalDateTime.now(), "Lab Bench 3", "Dr. Chen", true,
                "State Forensics Lab", 2, true, "Lab Report"
        );

        assertTrue(doc instanceof Evidence);
        assertTrue(doc instanceof InvestigationAsset);
        assertEquals("EVD-DOC", doc.getId());
        assertEquals("State Forensics Lab", doc.getAuthor());
        assertEquals(2, doc.getPageCount());
        assertTrue(doc.isClassified());

        String summary = doc.getSummary();
        assertTrue(summary.contains("[DOCUMENT EVIDENCE]"));
        assertTrue(summary.contains("Lab Analysis"));
        assertTrue(summary.contains("RESTRICTED/CONFIDENTIAL"));
        assertTrue(summary.contains("Dr. Chen"));
    }

    @Test
    @DisplayName("Digital evidence polymorphism and getSummary()")
    void testDigitalEvidence() {
        DigitalEvidence digital = new DigitalEvidence(
                "EVD-DIG", "USB Thumbdrive Image", "CASE-01", "Raw dd image of flash storage",
                LocalDateTime.now(), "Suspect Laptop Bag", "Agent Miller", true,
                "SanDisk 32GB Flash Drive", "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                "10.0.0.1", 33554432L
        );

        assertTrue(digital instanceof Evidence);
        assertTrue(digital instanceof InvestigationAsset);
        assertEquals("EVD-DIG", digital.getId());
        assertEquals("SanDisk 32GB Flash Drive", digital.getDeviceType());
        assertEquals(33554432L, digital.getFileSizeBytes());

        String summary = digital.getSummary();
        assertTrue(summary.contains("[DIGITAL EVIDENCE]"));
        assertTrue(summary.contains("USB Thumbdrive Image"));
        assertTrue(summary.contains("SHA-256"));
        assertTrue(summary.contains("32 MB"));
    }
}
