package com.mysteryvault.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

/**
 * Deterministic database seeder for MysteryVault.
 * Populates global catalog and all four original case databases with complete,
 * internally consistent investigation storylines.
 */
public class DatabasePopulator {

    public static void seedCatalog(Connection conn) throws SQLException {
        // Only seed if catalog is empty
        try (PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM cases_catalog");
             ResultSet rs = check.executeQuery()) {
            if (rs.next() && rs.getInt(1) > 0) {
                return;
            }
        }

        // Check if already seeded to prevent resetting user progress on restart
        try (PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM cases_catalog");
             ResultSet rs = check.executeQuery()) {
            if (rs.next() && rs.getInt(1) > 0) {
                return;
            }
        }

        conn.setAutoCommit(false);
        try {
            // Seed Cases
            String insertCase = "INSERT INTO cases_catalog (case_id, title, description, difficulty, db_filename, is_unlocked, is_completed) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertCase)) {
                // Case 101
                ps.setString(1, "CASE_101");
                ps.setString(2, "The Missing Diamond");
                ps.setString(3, "A 45-carat diamond vanishes from a private museum gala during a 3-minute power blackout.");
                ps.setString(4, "MEDIUM");
                ps.setString(5, "case_101.db");
                ps.setInt(6, 1);
                ps.setInt(7, 0);
                ps.executeUpdate();

                // Case 102
                ps.setString(1, "CASE_102");
                ps.setString(2, "The Library Code");
                ps.setString(3, "A confidential 16th-century cipher manuscript disappears from the university archive vault.");
                ps.setString(4, "HARD");
                ps.setString(5, "case_102.db");
                ps.setInt(6, 1);
                ps.setInt(7, 0);
                ps.executeUpdate();

                // Case 103
                ps.setString(1, "CASE_103");
                ps.setString(2, "The Poisoned Drink");
                ps.setString(3, "Lord Croft collapses at a private manor dinner after sipping customized vintage port.");
                ps.setString(4, "EASY");
                ps.setString(5, "case_103.db");
                ps.setInt(6, 1);
                ps.setInt(7, 0);
                ps.executeUpdate();

                // Case 104
                ps.setString(1, "CASE_104");
                ps.setString(2, "The Vanishing Prototype");
                ps.setString(3, "A revolutionary quantum robotics prototype disappears from a cleanroom bench before an investor demo.");
                ps.setString(4, "HARD");
                ps.setString(5, "case_104.db");
                ps.setInt(6, 1);
                ps.setInt(7, 0);
                ps.executeUpdate();
            }

            // Seed Initial Player Profile (Beginner: Rookie Detective with 0 XP)
            String insertProfile = "INSERT OR IGNORE INTO player_profile (profile_id, detective_name, experience_points, completed_cases, correct_deductions, total_deductions, best_score) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertProfile)) {
                ps.setString(1, "default_detective");
                ps.setString(2, "Detective Jack Malone");
                ps.setInt(3, 0); // 0 XP
                ps.setInt(4, 0); // 0 completed cases
                ps.setInt(5, 0); // 0 correct deductions
                ps.setInt(6, 0); // 0 total deductions
                ps.setInt(7, 0); // 0 best score
                ps.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public static void seedCaseDatabase(Connection conn, String caseId) throws SQLException {
        String normalized = CaseDatabaseManager.normalizeCaseId(caseId);
        // Only seed if suspects table is empty
        try (PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM suspects");
             ResultSet rs = check.executeQuery()) {
            if (rs.next() && rs.getInt(1) > 0) {
                return;
            }
        }

        conn.setAutoCommit(false);
        try {
            switch (normalized) {
                case "CASE_101":
                    seedCase101(conn);
                    break;
                case "CASE_102":
                    seedCase102(conn);
                    break;
                case "CASE_103":
                    seedCase103(conn);
                    break;
                case "CASE_104":
                    seedCase104(conn);
                    break;
                default:
                    seedCase101(conn);
                    break;
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // ==========================================
    // CASE 101: THE MISSING DIAMOND
    // ==========================================
    private static void seedCase101(Connection conn) throws SQLException {
        // Metadata
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO case_metadata (case_id, title, summary, status) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, "CASE_101");
            ps.setString(2, "The Missing Diamond");
            ps.setString(3, "The 45-carat Eye of Orion diamond vanished from the Elysium Museum during an evening exhibition blackout.");
            ps.setString(4, "OPEN");
            ps.executeUpdate();
        }

        // Canonical Solution (Private)
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO canonical_solution (culprit_id, culprit_name, motive, decisive_evidence_ids, explanation) VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, "SUS_101_3");
            ps.setString(2, "Marcus Vance");
            ps.setString(3, "Financial desperation to pay off loan shark syndicates using a black-market diamond broker");
            ps.setString(4, "EVD_101_SEC_LOG,EVD_101_GLASS_CUTTER");
            ps.setString(5, "Marcus Vance leveraged his security master badge to access the vault during the blackout and cut the display case using an engraved cutter.");
            ps.executeUpdate();
        }

        // Suspects
        String insertSuspect = "INSERT INTO suspects (suspect_id, name, role, occupation, age, alibi_statement, suspicion_level, is_prime_suspect) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSuspect)) {
            addSuspect(ps, "SUS_101_1", "Julian Drake", "Museum Curator", "Art Historian", 52, "I was checking the circuit breaker in the North Mezzanine with the lighting technician.", 45, 0);
            addSuspect(ps, "SUS_101_2", "Evelyn Sterling", "Gem Collector", "Socialite & Investor", 41, "I was speaking with the French Ambassador beside the central fountain when lights went dark.", 50, 0);
            addSuspect(ps, "SUS_101_3", "Marcus Vance", "Head of Security", "Ex-Military Officer", 38, "I stayed inside the CCTV monitoring booth monitoring camera status throughout the blackout.", 75, 1);
            addSuspect(ps, "SUS_101_4", "Lila Moreau", "Lighting Technician", "Electrical Specialist", 29, "The master circuit tripped spontaneously. I had to manually reset it in the utility cellar.", 40, 0);
            addSuspect(ps, "SUS_101_5", "Arthur Pendelton", "Senior Janitor", "Maintenance Staff", 61, "I was emptying recycling bins on the second floor gallery when the alarms triggered.", 30, 0);
        }

        // Evidence
        String insertEvidence = "INSERT INTO evidence (evidence_id, name, type, description, location_found, time_recorded, associated_person_id, significance, is_discovered) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertEvidence)) {
            addEvidence(ps, "EVD_101_SEC_LOG", "Vault RFID Access Log", "DIGITAL", "Server log showing Master Badge #04 was swiped at the vault door at 09:29 PM during the blackout.", "Security Server", "09:35 PM", "SUS_101_3", "HIGH", 1);
            addEvidence(ps, "EVD_101_GLASS_CUTTER", "Engraved Glass Cutter", "PHYSICAL", "Industrial diamond-tipped glass cutter engraved with initials 'M.V.' recovered from ventilation duct.", "HVAC Air Duct", "10:15 PM", "SUS_101_3", "CRITICAL", 1);
            addEvidence(ps, "EVD_101_CIRCUIT_BOX", "Tampered Breaker Switch", "PHYSICAL", "A bypass wire inserted into Breaker 4 caused a deliberate timed overload at 09:28 PM.", "Basement Utility Cellar", "09:50 PM", "SUS_101_4", "MEDIUM", 1);
            addEvidence(ps, "EVD_101_VELVET_LINING", "Torn Replica Velvet Pad", "PHYSICAL", "A faux velvet display cushion cut to exact dimensions of the Eye of Orion pedestal.", "Janitor's Closet", "10:30 PM", "SUS_101_5", "MEDIUM", 0);
            addEvidence(ps, "EVD_101_BROKER_NOTE", "Coded Broker Ledger", "DOCUMENT", "Encrypted handwritten ledger detailing a 2-million dollar transfer for a '45ct celestial stone'.", "Staff Locker 12", "11:00 PM", "SUS_101_3", "HIGH", 0);
        }

        // Clues
        String insertClue = "INSERT INTO clues (clue_id, title, description, status, prerequisite_evidence_ids) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertClue)) {
            addClue(ps, "CLUE_101_INSIDER", "Insider Security Override", "Only someone with Level 5 Security clearance could bypass the biometric pressure sensor.", "UNLOCKED", "EVD_101_SEC_LOG");
            addClue(ps, "CLUE_101_PREMEDITATED", "Calculated Power Blackout", "The power failure was not accidental; it was timed precisely to the 3-minute patrol gap.", "LOCKED", "EVD_101_CIRCUIT_BOX");
            addClue(ps, "CLUE_101_DECISIVE_TOOL", "The Cutter Matches Vance", "The cutter's blade angle perfectly matches the circular incision in the display pedestal glass.", "LOCKED", "EVD_101_GLASS_CUTTER,EVD_101_SEC_LOG");
        }

        // Timeline
        String insertTimeline = "INSERT INTO timeline_events (event_id, timestamp_str, sort_order, description, person_id, location, is_verified, has_conflict, conflict_details) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertTimeline)) {
            addTimeline(ps, "TL_101_1", "08:30 PM", 1, "Gala attendees enter museum and admire the Eye of Orion in the Central Rotunda.", "SUS_101_1", "Central Rotunda", 1, 0, null);
            addTimeline(ps, "TL_101_2", "09:15 PM", 2, "Marcus Vance conducts an unscheduled sensor calibration on the vault doors.", "SUS_101_3", "East Vault Corridor", 1, 0, null);
            addTimeline(ps, "TL_101_3", "09:28 PM", 3, "Main gallery lighting goes completely dark due to breaker trip.", "SUS_101_4", "Utility Cellar", 1, 0, null);
            addTimeline(ps, "TL_101_4", "09:29 PM", 4, "Marcus Vance claims he was in the control booth, but his RFID badge registers at the vault door.", "SUS_101_3", "Security Vault", 0, 1, "Conflict: Vance claimed he never left the control booth between 09:25 and 09:35 PM.");
            addTimeline(ps, "TL_101_5", "09:31 PM", 5, "Emergency generator kicks in; glass alarm rings. Diamond is missing.", "SUS_101_1", "Central Rotunda", 1, 0, null);
        }

        // Interrogation
        String insertDialogue = "INSERT INTO interrogation_dialogues (dialogue_id, suspect_id, question_text, response_text, is_asked, extracted_statement_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertDialogue)) {
            addDialogue(ps, "DLG_101_1", "SUS_101_3", "Where were you during the 3-minute blackout?", "I was locked inside the security control booth trying to switch to backup monitor feeds. I never left.", 0, "STMT_101_1");
            addDialogue(ps, "DLG_101_2", "SUS_101_3", "Can you explain why your RFID badge accessed the vault at 09:29 PM?", "That badge was cloned or someone grabbed my spare lanyard from the charging dock!", 0, "STMT_101_2");
            addDialogue(ps, "DLG_101_3", "SUS_101_1", "Did you notice anything unusual before the lights went out?", "Marcus Vance was acting nervous and ordered the junior guards to take early meal breaks at 09:00 PM.", 0, "STMT_101_3");
            addDialogue(ps, "DLG_101_4", "SUS_101_2", "Did you see anyone near the exhibit corridor?", "I saw a tall figure in a security uniform carrying a black tool pouch heading toward the emergency exit.", 0, "STMT_101_4");
        }

        // Statements
        String insertStatement = "INSERT INTO statements (statement_id, person_id, statement_text, time_recorded, extracted_times, extracted_locations) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertStatement)) {
            addStatement(ps, "STMT_101_1", "SUS_101_3", "I never left the CCTV booth between 09:25 PM and 09:35 PM.", "09:40 PM", "09:25 PM, 09:35 PM", "CCTV Booth");
            addStatement(ps, "STMT_101_2", "SUS_101_3", "My Master RFID badge must have been cloned by someone else.", "10:10 PM", "09:29 PM", "East Vault");
            addStatement(ps, "STMT_101_3", "SUS_101_1", "Vance dismissed the exterior guards at 09:00 PM citing budget cuts.", "10:20 PM", "09:00 PM", "Museum Grounds");
        }

        // Contradictions
        String insertContradiction = "INSERT INTO contradictions (contradiction_id, title, description, suspect_id, evidence_id, timeline_event_id, severity, is_resolved) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertContradiction)) {
            addContradiction(ps, "CONTR_101_1", "Vance Booth Alibi False", "Vance claimed he stayed in the CCTV booth, but server RFID logs record his badge opening the vault at 09:29 PM.", "SUS_101_3", "EVD_101_SEC_LOG", "TL_101_4", "CRITICAL", 0);
            addContradiction(ps, "CONTR_101_2", "Cloned Badge Defense Disproved", "The vault reader records biometric thumbprint verification alongside the RFID swipe, disproving the stolen lanyard claim.", "SUS_101_3", "EVD_101_GLASS_CUTTER", "TL_101_4", "HIGH", 0);
        }

        // Connections
        String insertConnection = "INSERT INTO connections (connection_id, source_id, source_label, target_id, target_label, relationship_type, weight) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertConnection)) {
            addConnection(ps, "CONN_101_1", "SUS_101_3", "Marcus Vance", "EVD_101_SEC_LOG", "RFID Log", "ACCUSES", 0.9);
            addConnection(ps, "CONN_101_2", "SUS_101_3", "Marcus Vance", "EVD_101_GLASS_CUTTER", "Glass Cutter", "OWNS", 1.0);
            addConnection(ps, "CONN_101_3", "SUS_101_4", "Lila Moreau", "EVD_101_CIRCUIT_BOX", "Circuit Breaker", "ASSOCIATED_WITH", 0.6);
            addConnection(ps, "CONN_101_4", "SUS_101_1", "Julian Drake", "SUS_101_3", "Marcus Vance", "ACCUSES", 0.8);
        }
    }

    // ==========================================
    // CASE 102: THE LIBRARY CODE
    // ==========================================
    private static void seedCase102(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO case_metadata (case_id, title, summary, status) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, "CASE_102");
            ps.setString(2, "The Library Code");
            ps.setString(3, "A 16th-century cipher manuscript vanished from the university archives before its donation ceremony.");
            ps.setString(4, "OPEN");
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO canonical_solution (culprit_id, culprit_name, motive, decisive_evidence_ids, explanation) VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, "SUS_102_1");
            ps.setString(2, "Dr. Harrison Vance");
            ps.setString(3, "Academic fraud concealment: Marginal notes in the cipher proved Vance plagiarized his magnum opus");
            ps.setString(4, "EVD_102_SPECTACLE_FIBER,EVD_102_BURNED_PAGE");
            ps.setString(5, "Dr. Vance slipped away from his lecture early, entered the archive using emergency override keys, and partially destroyed the marginal notes in his office fireplace.");
            ps.executeUpdate();
        }

        String insertSuspect = "INSERT INTO suspects (suspect_id, name, role, occupation, age, alibi_statement, suspicion_level, is_prime_suspect) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSuspect)) {
            addSuspect(ps, "SUS_102_1", "Dr. Harrison Vance", "Senior Professor", "Paleographer", 59, "I was delivering a keynote lecture in Hall C until 05:30 PM without interruption.", 70, 1);
            addSuspect(ps, "SUS_102_2", "Elena Rostova", "Visiting Scholar", "Cryptanalyst", 34, "I was in the reading room transcribing folio 14 under supervisory camera observation.", 35, 0);
            addSuspect(ps, "SUS_102_3", "Simon Finch", "Graduate Assistant", "Archival Fellow", 26, "I was cataloging microfilm in the annex and taking inventory delivery boxes.", 40, 0);
            addSuspect(ps, "SUS_102_4", "Cordelia Cross", "Trustee & Donor", "Philanthropist", 64, "I arrived at 05:15 PM and waited in the dean's private lounge for the ceremony to begin.", 30, 0);
            addSuspect(ps, "SUS_102_5", "Tobias Reed", "Head Archivist", "Rare Book Curator", 48, "I locked the display safe at 04:30 PM and had afternoon tea with Dean Abernathy.", 25, 0);
        }

        String insertEvidence = "INSERT INTO evidence (evidence_id, name, type, description, location_found, time_recorded, associated_person_id, significance, is_discovered) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertEvidence)) {
            addEvidence(ps, "EVD_102_SPECTACLE_FIBER", "Green Tweed Fabric Fiber", "PHYSICAL", "Thread of Scottish green herringbone tweed snagged on the vault safe combination dial.", "Archive Safe", "05:45 PM", "SUS_102_1", "CRITICAL", 1);
            addEvidence(ps, "EVD_102_BURNED_PAGE", "Scorched Vellum Fragment", "PHYSICAL", "Singed corner of 16th-century vellum containing Latin marginalia found in faculty office grate.", "Faculty Office 3B", "06:15 PM", "SUS_102_1", "CRITICAL", 1);
            addEvidence(ps, "EVD_102_LECTURE_LOG", "Lecture Hall C Badge Terminal", "DIGITAL", "Student attendance scans show Vance's lecture was dismissed at 04:20 PM, not 05:30 PM.", "Hall C Portal", "05:50 PM", "SUS_102_1", "HIGH", 1);
            addEvidence(ps, "EVD_102_CIPHER_NOTES", "Decoded Marginalia Copy", "DOCUMENT", "Notes demonstrating the cipher was authored by Brother Anselm, not Vance's alleged source.", "Reading Desk 4", "05:00 PM", "SUS_102_2", "MEDIUM", 0);
        }

        String insertClue = "INSERT INTO clues (clue_id, title, description, status, prerequisite_evidence_ids) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertClue)) {
            addClue(ps, "CLUE_102_ALIBI_COLLAPSE", "False Keynote Alibi", "Dr. Vance claimed to lecture until 05:30 PM, but the hall was empty by 04:25 PM.", "UNLOCKED", "EVD_102_LECTURE_LOG");
            addClue(ps, "CLUE_102_VAULT_BREACH", "Safe Was Opened Without Forced Entry", "The combination was dialed smoothly; only three faculty members memorized the sequence.", "LOCKED", "EVD_102_SPECTACLE_FIBER");
            addClue(ps, "CLUE_102_DESPERATE_ARSON", "Desecration of Antiquity", "The manuscript was burned to destroy evidence of academic plagiarism.", "LOCKED", "EVD_102_BURNED_PAGE,EVD_102_SPECTACLE_FIBER");
        }

        String insertTimeline = "INSERT INTO timeline_events (event_id, timestamp_str, sort_order, description, person_id, location, is_verified, has_conflict, conflict_details) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertTimeline)) {
            addTimeline(ps, "TL_102_1", "04:00 PM", 1, "Dr. Vance begins lecture on Medieval Cryptography in Hall C.", "SUS_102_1", "Hall C", 1, 0, null);
            addTimeline(ps, "TL_102_2", "04:20 PM", 2, "Dr. Vance dismisses class 70 minutes early citing sudden headache.", "SUS_102_1", "Hall C", 1, 0, null);
            addTimeline(ps, "TL_102_3", "04:45 PM", 3, "Dr. Vance enters Archive Vault using faculty bypass.", "SUS_102_1", "Rare Archive", 0, 1, "Conflict: Vance claimed he was lecturing until 05:30 PM.");
            addTimeline(ps, "TL_102_4", "05:35 PM", 4, "Ceremony guests assemble; safe is found open and empty.", "SUS_102_5", "Rare Archive", 1, 0, null);
        }

        String insertDialogue = "INSERT INTO interrogation_dialogues (dialogue_id, suspect_id, question_text, response_text, is_asked, extracted_statement_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertDialogue)) {
            addDialogue(ps, "DLG_102_1", "SUS_102_1", "Did you remain in Hall C until 05:30 PM?", "Of course! Eighty students will attest to my comprehensive discourse on the Venetian ciphers.", 0, "STMT_102_1");
            addDialogue(ps, "DLG_102_2", "SUS_102_1", "Can you explain the green tweed thread found on the safe dial?", "Green tweed is common faculty attire! Many colleagues wear Harris Tweed jackets.", 0, "STMT_102_2");
            addDialogue(ps, "DLG_102_3", "SUS_102_3", "Did you see Dr. Vance after class?", "I saw him rushing past the archives stairwell at 04:35 PM carrying a leather folio.", 0, "STMT_102_3");
        }

        String insertStatement = "INSERT INTO statements (statement_id, person_id, statement_text, time_recorded, extracted_times, extracted_locations) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertStatement)) {
            addStatement(ps, "STMT_102_1", "SUS_102_1", "I never stepped out of Hall C between 04:00 PM and 05:30 PM.", "05:40 PM", "04:00 PM, 05:30 PM", "Hall C");
            addStatement(ps, "STMT_102_2", "SUS_102_1", "My jacket was hanging in my faculty office all afternoon.", "06:00 PM", "Afternoon", "Faculty Office");
            addStatement(ps, "STMT_102_3", "SUS_102_3", "Dr. Vance dismissed class at 04:20 PM and carried a bulky satchel.", "06:10 PM", "04:20 PM", "Stairwell");
        }

        String insertContradiction = "INSERT INTO contradictions (contradiction_id, title, description, suspect_id, evidence_id, timeline_event_id, severity, is_resolved) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertContradiction)) {
            addContradiction(ps, "CONTR_102_1", "Hall C Attendance Conflict", "Vance swore he lectured until 05:30 PM, but automated student scanners prove the room was cleared at 04:20 PM.", "SUS_102_1", "EVD_102_LECTURE_LOG", "TL_102_3", "CRITICAL", 0);
            addContradiction(ps, "CONTR_102_2", "Fabric Match Contradiction", "Vance claimed his tweed jacket remained in his office, but distinctive fibers were snagged on the safe dial.", "SUS_102_1", "EVD_102_SPECTACLE_FIBER", "TL_102_3", "HIGH", 0);
        }

        String insertConnection = "INSERT INTO connections (connection_id, source_id, source_label, target_id, target_label, relationship_type, weight) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertConnection)) {
            addConnection(ps, "CONN_102_1", "SUS_102_1", "Dr. Harrison Vance", "EVD_102_BURNED_PAGE", "Burned Fragment", "OWNS", 1.0);
            addConnection(ps, "CONN_102_2", "SUS_102_1", "Dr. Harrison Vance", "EVD_102_SPECTACLE_FIBER", "Tweed Fiber", "OWNS", 0.9);
            addConnection(ps, "CONN_102_3", "SUS_102_3", "Simon Finch", "SUS_102_1", "Dr. Harrison Vance", "ACCUSES", 0.8);
        }
    }

    // ==========================================
    // CASE 103: THE POISONED DRINK
    // ==========================================
    private static void seedCase103(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO case_metadata (case_id, title, summary, status) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, "CASE_103");
            ps.setString(2, "The Poisoned Drink");
            ps.setString(3, "Lord Croft collapses at a private manor dinner party after taking a sip from his crystal port glass.");
            ps.setString(4, "OPEN");
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO canonical_solution (culprit_id, culprit_name, motive, decisive_evidence_ids, explanation) VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, "SUS_103_3");
            ps.setString(2, "Vivienne Sinclair");
            ps.setString(3, "Preventing exposure of £1.2M company embezzlement which Lord Croft was scheduled to audit the next morning");
            ps.setString(4, "EVD_103_COCKTAIL_GLASS,EVD_103_POWDER_COMPACT");
            ps.setString(5, "Vivienne slipped into the service butler pantry at 08:38 PM under guise of freshening up, dusting Lord Croft's personalized crystal rim with a fast-acting synthetic sedative.");
            ps.executeUpdate();
        }

        String insertSuspect = "INSERT INTO suspects (suspect_id, name, role, occupation, age, alibi_statement, suspicion_level, is_prime_suspect) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSuspect)) {
            addSuspect(ps, "SUS_103_1", "Damian Croft", "Estranged Son", "Gambler & Investor", 28, "I was smoking a cigar on the veranda from 08:30 PM until the commotion occurred.", 65, 0);
            addSuspect(ps, "SUS_103_2", "Dr. Eleanor Vance", "Family Physician", "Cardiologist", 47, "I was discussing Lord Croft's blood pressure prescription with Gerald the butler.", 45, 0);
            addSuspect(ps, "SUS_103_3", "Vivienne Sinclair", "Business Partner", "Managing Director", 39, "I sat beside Beatrice Croft at the dining table the entire time, discussing charity gala tickets.", 80, 1);
            addSuspect(ps, "SUS_103_4", "Gerald Finch", "Head Butler", "Estate Staff", 63, "I decanted the 1982 Fonseca port in the pantry and served each guest in turn.", 35, 0);
            addSuspect(ps, "SUS_103_5", "Beatrice Croft", "Niece", "Art Restorer", 24, "I was seated at the table playing Chopin on the salon piano until dessert was brought.", 20, 0);
        }

        String insertEvidence = "INSERT INTO evidence (evidence_id, name, type, description, location_found, time_recorded, associated_person_id, significance, is_discovered) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertEvidence)) {
            addEvidence(ps, "EVD_103_COCKTAIL_GLASS", "Lord Croft's Crystal Glass", "FORENSIC", "Trace residue of concentrated synthetic sedative compound found coated on the interior rim.", "Dining Table", "08:50 PM", "SUS_103_3", "CRITICAL", 1);
            addEvidence(ps, "EVD_103_POWDER_COMPACT", "Silver Cosmetic Compact", "PHYSICAL", "Luxury cosmetic compact with a hollowed underside containing microscopic residue of the sedative.", "Vivienne's Handbag", "09:15 PM", "SUS_103_3", "CRITICAL", 1);
            addEvidence(ps, "EVD_103_PORT_DECANTER", "Crystal Wine Decanter", "PHYSICAL", "The bulk decanter was tested completely clean; only Lord Croft's individual glass was tainted.", "Service Pantry", "08:55 PM", "SUS_103_4", "HIGH", 1);
            addEvidence(ps, "EVD_103_AUDIT_FILE", "Forensic Audit Subpoena", "DOCUMENT", "Confidential letters revealing Lord Croft had discovered massive fund misappropriation by Vivienne.", "Lord Croft's Study", "09:30 PM", "SUS_103_3", "HIGH", 0);
        }

        String insertClue = "INSERT INTO clues (clue_id, title, description, status, prerequisite_evidence_ids) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertClue)) {
            addClue(ps, "CLUE_103_TARGETED_ATTACK", "Targeted Glass Contamination", "Since the decanter was untainted, the compound was applied directly to Croft's glass rim.", "UNLOCKED", "EVD_103_COCKTAIL_GLASS,EVD_103_PORT_DECANTER");
            addClue(ps, "CLUE_103_PHYSICAL_LINK", "Sedative Source Identified", "The powder in the cosmetic compact matches the chemical fingerprint on the glass rim.", "LOCKED", "EVD_103_POWDER_COMPACT,EVD_103_COCKTAIL_GLASS");
            addClue(ps, "CLUE_103_FINANCIAL_MOTIVE", "Desperate Embezzlement Coverup", "Vivienne stood to face criminal charges if Lord Croft submitted the audit report the next morning.", "LOCKED", "EVD_103_AUDIT_FILE");
        }

        String insertTimeline = "INSERT INTO timeline_events (event_id, timestamp_str, sort_order, description, person_id, location, is_verified, has_conflict, conflict_details) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertTimeline)) {
            addTimeline(ps, "TL_103_1", "08:15 PM", 1, "Guests finish the main course; butler Gerald pours decanted port in the pantry.", "SUS_103_4", "Service Pantry", 1, 0, null);
            addTimeline(ps, "TL_103_2", "08:35 PM", 2, "Gerald steps into the wine cellar to fetch dessert glasses.", "SUS_103_4", "Wine Cellar", 1, 0, null);
            addTimeline(ps, "TL_103_3", "08:38 PM", 3, "Vivienne claims she remained seated, but Beatrice witnessed her enter the pantry corridor.", "SUS_103_3", "Pantry Corridor", 0, 1, "Conflict: Vivienne claimed she sat beside Beatrice continuously between 08:30 and 08:45 PM.");
            addTimeline(ps, "TL_103_4", "08:42 PM", 4, "Port glasses served to Lord Croft and guests.", "SUS_103_4", "Dining Room", 1, 0, null);
            addTimeline(ps, "TL_103_5", "08:45 PM", 5, "Lord Croft toasts the table, sips port, and collapses within 60 seconds.", "SUS_103_1", "Dining Room", 1, 0, null);
        }

        String insertDialogue = "INSERT INTO interrogation_dialogues (dialogue_id, suspect_id, question_text, response_text, is_asked, extracted_statement_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertDialogue)) {
            addDialogue(ps, "DLG_103_1", "SUS_103_3", "Did you leave the dining table at any point before dessert?", "Never! Beatrice Croft and I were chatting about the symphony gala the entire evening.", 0, "STMT_103_1");
            addDialogue(ps, "DLG_103_2", "SUS_103_5", "Can you confirm Vivienne stayed seated beside you?", "Actually, Vivienne excused herself around 08:37 PM saying she needed a powder room mirror.", 0, "STMT_103_2");
            addDialogue(ps, "DLG_103_3", "SUS_103_3", "How do you explain the chemical trace inside your compact?", "I... I bought that compact from an antique flea market in Milan! Someone must have planted it!", 0, "STMT_103_3");
            addDialogue(ps, "DLG_103_4", "SUS_103_4", "Was the pantry door unattended?", "Only for two minutes when I retrieved the port corkscrew from the cellar.", 0, "STMT_103_4");
        }

        String insertStatement = "INSERT INTO statements (statement_id, person_id, statement_text, time_recorded, extracted_times, extracted_locations) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertStatement)) {
            addStatement(ps, "STMT_103_1", "SUS_103_3", "I stayed at the dining table with Beatrice from 08:30 PM until 08:45 PM.", "09:00 PM", "08:30 PM, 08:45 PM", "Dining Room");
            addStatement(ps, "STMT_103_2", "SUS_103_5", "Vivienne was absent from her seat between 08:37 PM and 08:41 PM.", "09:10 PM", "08:37 PM, 08:41 PM", "Dining Room");
            addStatement(ps, "STMT_103_3", "SUS_103_3", "The silver compact is a vintage heirloom; I never opened the bottom seal.", "09:20 PM", "Evening", "Manor");
        }

        String insertContradiction = "INSERT INTO contradictions (contradiction_id, title, description, suspect_id, evidence_id, timeline_event_id, severity, is_resolved) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertContradiction)) {
            addContradiction(ps, "CONTR_103_1", "Vivienne Table Alibi Contradicted", "Vivienne swore she sat with Beatrice uninterrupted, but Beatrice confirmed Vivienne vacated her chair at 08:37 PM.", "SUS_103_3", null, "TL_103_3", "CRITICAL", 0);
            addContradiction(ps, "CONTR_103_2", "Compact Compound Chemical Match", "Vivienne claimed ignorance of the compound, but laboratory swabs prove identical molecular signatures on her compact and Croft's glass.", "SUS_103_3", "EVD_103_POWDER_COMPACT", "TL_103_5", "HIGH", 0);
        }

        String insertConnection = "INSERT INTO connections (connection_id, source_id, source_label, target_id, target_label, relationship_type, weight) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertConnection)) {
            addConnection(ps, "CONN_103_1", "SUS_103_3", "Vivienne Sinclair", "EVD_103_POWDER_COMPACT", "Silver Compact", "OWNS", 1.0);
            addConnection(ps, "CONN_103_2", "SUS_103_3", "Vivienne Sinclair", "EVD_103_COCKTAIL_GLASS", "Tainted Glass", "ACCUSES", 0.9);
            addConnection(ps, "CONN_103_3", "SUS_103_5", "Beatrice Croft", "SUS_103_3", "Vivienne Sinclair", "CONTRADICTS", 0.85);
            addConnection(ps, "CONN_103_4", "SUS_103_4", "Gerald Finch", "EVD_103_PORT_DECANTER", "Port Decanter", "ASSOCIATED_WITH", 0.7);
        }
    }

    // ==========================================
    // CASE 104: THE VANISHING PROTOTYPE
    // ==========================================
    private static void seedCase104(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO case_metadata (case_id, title, summary, status) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, "CASE_104");
            ps.setString(2, "The Vanishing Prototype");
            ps.setString(3, "A high-density quantum robotics core vanished from a cleanroom test bench prior to an executive demonstration.");
            ps.setString(4, "OPEN");
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO canonical_solution (culprit_id, culprit_name, motive, decisive_evidence_ids, explanation) VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, "SUS_104_2");
            ps.setString(2, "Kira Patel");
            ps.setString(3, "Corporate espionage: Selling proprietary core telemetry and physical prototype to rival syndicate TitanCorp");
            ps.setString(4, "EVD_104_FARADAY_BAG,EVD_104_FLASH_DRIVE");
            ps.setString(5, "Kira used an RF-shielded Faraday pouch to conceal the core's live GPS beacon, smuggle it past the cleanroom metal detector, and store telemetry on an encrypted flash drive.");
            ps.executeUpdate();
        }

        String insertSuspect = "INSERT INTO suspects (suspect_id, name, role, occupation, age, alibi_statement, suspicion_level, is_prime_suspect) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSuspect)) {
            addSuspect(ps, "SUS_104_1", "Dr. Aris Thorne", "Chief Scientist", "Quantum Physicist", 54, "I was preparing presentation slides in Conference Room B from 06:00 PM to 07:00 PM.", 45, 0);
            addSuspect(ps, "SUS_104_2", "Kira Patel", "Lead Firmware Dev", "Systems Engineer", 31, "I was having an energy drink in the cafeteria debugging motor drivers on my personal laptop.", 75, 1);
            addSuspect(ps, "SUS_104_3", "Nolan Vance", "Safety Officer", "Environmental Engineer", 46, "I was checking hazardous chemical scrubbers on the roof intake manifold.", 35, 0);
            addSuspect(ps, "SUS_104_4", "Maya Lin", "Hardware Architect", "Robotics Designer", 27, "I left early at 05:45 PM to pick up sample casings from the rapid prototyping facility.", 40, 0);
            addSuspect(ps, "SUS_104_5", "Victor Sterling", "CEO", "Tech Executive", 51, "I was hosting potential European venture capital investors in the executive suite.", 50, 0);
        }

        String insertEvidence = "INSERT INTO evidence (evidence_id, name, type, description, location_found, time_recorded, associated_person_id, significance, is_discovered) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertEvidence)) {
            addEvidence(ps, "EVD_104_FARADAY_BAG", "Anti-Static RF Shielding Pouch", "PHYSICAL", "Military-grade Faraday pouch stamped with Kira's workstation asset serial recovered from ventilation shaft.", "Server Rack Exhaust", "07:15 PM", "SUS_104_2", "CRITICAL", 1);
            addEvidence(ps, "EVD_104_FLASH_DRIVE", "Encrypted High-Speed USB", "DIGITAL", "Encrypted USB drive containing raw quantum core telemetry and firmware source code discovered in Kira's car.", "Parking Level B2", "07:45 PM", "SUS_104_2", "CRITICAL", 1);
            addEvidence(ps, "EVD_104_AIRLOCK_LOG", "Cleanroom Badge Terminal", "DIGITAL", "Airlock biometric badge scanner recorded Kira's keycard entry at 06:22 PM and exit at 06:38 PM.", "Cleanroom Gate 2", "06:55 PM", "SUS_104_2", "HIGH", 1);
            addEvidence(ps, "EVD_104_TITANCORP_OFFER", "Syndicate Offer Letter", "DOCUMENT", "Offer letter promising $1.5M sign-on bonus upon delivery of functional core prototype.", "Encrypted Email Cache", "08:00 PM", "SUS_104_2", "HIGH", 0);
        }

        String insertClue = "INSERT INTO clues (clue_id, title, description, status, prerequisite_evidence_ids) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertClue)) {
            addClue(ps, "CLUE_104_RF_BLOCKER", "Beacon Signal Jammed", "The quantum core's active telemetry pulse vanished precisely when bagged in RF shielding.", "UNLOCKED", "EVD_104_FARADAY_BAG");
            addClue(ps, "CLUE_104_CLEANROOM_BREACH", "Airlock Log Confirms Presence", "Kira claimed she remained in the cafeteria, but biometric airlock logs record cleanroom presence.", "LOCKED", "EVD_104_AIRLOCK_LOG");
            addClue(ps, "CLUE_104_ESPIONAGE_PAYOFF", "Proof of Industrial Theft", "Extracted code on the flash drive matched TitanCorp technical specifications.", "LOCKED", "EVD_104_FLASH_DRIVE,EVD_104_FARADAY_BAG");
        }

        String insertTimeline = "INSERT INTO timeline_events (event_id, timestamp_str, sort_order, description, person_id, location, is_verified, has_conflict, conflict_details) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertTimeline)) {
            addTimeline(ps, "TL_104_1", "06:00 PM", 1, "Quantum core completes final test cycle on cleanroom bench #3.", "SUS_104_1", "Cleanroom", 1, 0, null);
            addTimeline(ps, "TL_104_2", "06:18 PM", 2, "RF signal from quantum core drops off the network unexpectedly.", "SUS_104_2", "Cleanroom", 1, 0, null);
            addTimeline(ps, "TL_104_3", "06:22 PM", 3, "Kira Patel badges into Cleanroom Airlock while claiming to be in cafeteria.", "SUS_104_2", "Cleanroom Gate", 0, 1, "Conflict: Kira stated she was in the cafeteria debugging firmware from 06:10 to 06:50 PM.");
            addTimeline(ps, "TL_104_4", "06:45 PM", 4, "Executive demo team enters cleanroom; test bench is bare.", "SUS_104_5", "Cleanroom", 1, 0, null);
        }

        String insertDialogue = "INSERT INTO interrogation_dialogues (dialogue_id, suspect_id, question_text, response_text, is_asked, extracted_statement_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertDialogue)) {
            addDialogue(ps, "DLG_104_1", "SUS_104_2", "Where were you between 06:15 PM and 06:45 PM?", "I was sitting in the third-floor cafeteria sipping matcha latte and rewriting motor controller routines.", 0, "STMT_104_1");
            addDialogue(ps, "DLG_104_2", "SUS_104_2", "Why does the airlock log record your entry at 06:22 PM?", "Someone must have spoofed my RFID card with an NFC repeater!", 0, "STMT_104_2");
            addDialogue(ps, "DLG_104_3", "SUS_104_1", "Who had access to RF shielding materials?", "Kira ordered customized Faraday pouches last Tuesday for magnetic interference testing.", 0, "STMT_104_3");
        }

        String insertStatement = "INSERT INTO statements (statement_id, person_id, statement_text, time_recorded, extracted_times, extracted_locations) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertStatement)) {
            addStatement(ps, "STMT_104_1", "SUS_104_2", "I remained in the cafeteria between 06:10 PM and 06:50 PM without leaving.", "07:00 PM", "06:10 PM, 06:50 PM", "Cafeteria");
            addStatement(ps, "STMT_104_2", "SUS_104_2", "My RFID card was left on my desk during lunch; it might have been cloned.", "07:20 PM", "Lunch", "Desk");
            addStatement(ps, "STMT_104_3", "SUS_104_1", "Kira was in charge of the prototype shielding protocols.", "07:30 PM", "Tuesday", "Cleanroom");
        }

        String insertContradiction = "INSERT INTO contradictions (contradiction_id, title, description, suspect_id, evidence_id, timeline_event_id, severity, is_resolved) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertContradiction)) {
            addContradiction(ps, "CONTR_104_1", "Cafeteria Alibi Defeated by Airlock Logs", "Kira swore she was in the cafeteria, but biometric airlock logs record her entering cleanroom at 06:22 PM.", "SUS_104_2", "EVD_104_AIRLOCK_LOG", "TL_104_3", "CRITICAL", 0);
            addContradiction(ps, "CONTR_104_2", "RF Shielding Asset Serial Match", "Kira claimed no involvement in the theft, but the Faraday pouch found in the server vent bore her personal asset tag.", "SUS_104_2", "EVD_104_FARADAY_BAG", "TL_104_2", "HIGH", 0);
        }

        String insertConnection = "INSERT INTO connections (connection_id, source_id, source_label, target_id, target_label, relationship_type, weight) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertConnection)) {
            addConnection(ps, "CONN_104_1", "SUS_104_2", "Kira Patel", "EVD_104_FARADAY_BAG", "Faraday Pouch", "OWNS", 1.0);
            addConnection(ps, "CONN_104_2", "SUS_104_2", "Kira Patel", "EVD_104_FLASH_DRIVE", "Flash Drive", "OWNS", 0.95);
            addConnection(ps, "CONN_104_3", "SUS_104_1", "Dr. Aris Thorne", "SUS_104_2", "Kira Patel", "ACCUSES", 0.8);
        }
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================
    private static void addSuspect(PreparedStatement ps, String id, String name, String role, String occ, int age, String alibi, int susp, int prime) throws SQLException {
        ps.setString(1, id);
        ps.setString(2, name);
        ps.setString(3, role);
        ps.setString(4, occ);
        ps.setInt(5, age);
        ps.setString(6, alibi);
        ps.setInt(7, susp);
        ps.setInt(8, prime);
        ps.executeUpdate();
    }

    private static void addEvidence(PreparedStatement ps, String id, String name, String type, String desc, String loc, String time, String personId, String sig, int disc) throws SQLException {
        ps.setString(1, id);
        ps.setString(2, name);
        ps.setString(3, type);
        ps.setString(4, desc);
        ps.setString(5, loc);
        ps.setString(6, time);
        ps.setString(7, personId);
        ps.setString(8, sig);
        ps.setInt(9, disc);
        ps.executeUpdate();
    }

    private static void addClue(PreparedStatement ps, String id, String title, String desc, String status, String prereqs) throws SQLException {
        ps.setString(1, id);
        ps.setString(2, title);
        ps.setString(3, desc);
        ps.setString(4, status);
        ps.setString(5, prereqs);
        ps.executeUpdate();
    }

    private static void addTimeline(PreparedStatement ps, String id, String timeStr, int sort, String desc, String personId, String loc, int verified, int conflict, String conflictDetails) throws SQLException {
        ps.setString(1, id);
        ps.setString(2, timeStr);
        ps.setInt(3, sort);
        ps.setString(4, desc);
        ps.setString(5, personId);
        ps.setString(6, loc);
        ps.setInt(7, verified);
        ps.setInt(8, conflict);
        ps.setString(9, conflictDetails);
        ps.executeUpdate();
    }

    private static void addDialogue(PreparedStatement ps, String id, String suspectId, String q, String r, int asked, String stmtId) throws SQLException {
        ps.setString(1, id);
        ps.setString(2, suspectId);
        ps.setString(3, q);
        ps.setString(4, r);
        ps.setInt(5, asked);
        ps.setString(6, stmtId);
        ps.executeUpdate();
    }

    private static void addStatement(PreparedStatement ps, String id, String personId, String text, String time, String extTimes, String extLocs) throws SQLException {
        ps.setString(1, id);
        ps.setString(2, personId);
        ps.setString(3, text);
        ps.setString(4, time);
        ps.setString(5, extTimes);
        ps.setString(6, extLocs);
        ps.executeUpdate();
    }

    private static void addContradiction(PreparedStatement ps, String id, String title, String desc, String suspectId, String evidenceId, String timelineId, String severity, int resolved) throws SQLException {
        ps.setString(1, id);
        ps.setString(2, title);
        ps.setString(3, desc);
        ps.setString(4, suspectId);
        ps.setString(5, evidenceId);
        ps.setString(6, timelineId);
        ps.setString(7, severity);
        ps.setInt(8, resolved);
        ps.executeUpdate();
    }

    private static void addConnection(PreparedStatement ps, String id, String srcId, String srcLabel, String tgtId, String tgtLabel, String relType, double weight) throws SQLException {
        ps.setString(1, id);
        ps.setString(2, srcId);
        ps.setString(3, srcLabel);
        ps.setString(4, tgtId);
        ps.setString(5, tgtLabel);
        ps.setString(6, relType);
        ps.setDouble(7, weight);
        ps.executeUpdate();
    }
}
