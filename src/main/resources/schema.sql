-- ============================================================================
-- Mystery Vault Database Schema
-- Modules 1, 2, and 3: Cases, Suspects, Evidence, Clues & Archives
-- Compatible with PostgreSQL and ANSI-compliant SQL databases
-- ============================================================================

-- 1. Cases Table (Module 1)
CREATE TABLE IF NOT EXISTS cases (
    id VARCHAR(64) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    priority VARCHAR(32) NOT NULL DEFAULT 'MEDIUM',
    lead_investigator VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_cases_status ON cases(status);

-- 2. Suspects & Persons Table (Module 2)
CREATE TABLE IF NOT EXISTS suspects (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(64) NOT NULL DEFAULT 'Suspect',
    notes TEXT,
    occupation VARCHAR(255),
    age INT,
    alibi TEXT,
    suspicion_score INT,
    motive_description TEXT,
    alibi_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_suspect_age CHECK (age IS NULL OR (age >= 0 AND age <= 150)),
    CONSTRAINT chk_suspect_suspicion CHECK (suspicion_score IS NULL OR (suspicion_score >= 0 AND suspicion_score <= 100))
);

CREATE INDEX IF NOT EXISTS idx_suspects_name ON suspects(name);

-- 3. Case-Suspect Association (Module 2)
CREATE TABLE IF NOT EXISTS case_suspects (
    case_id VARCHAR(64) NOT NULL REFERENCES cases(id) ON DELETE CASCADE,
    suspect_id VARCHAR(64) NOT NULL REFERENCES suspects(id) ON DELETE CASCADE,
    PRIMARY KEY (case_id, suspect_id)
);

CREATE INDEX IF NOT EXISTS idx_case_suspects_case ON case_suspects(case_id);
CREATE INDEX IF NOT EXISTS idx_case_suspects_suspect ON case_suspects(suspect_id);

-- 4. Evidence Table (Module 3 - Polymorphic Document & Digital)
CREATE TABLE IF NOT EXISTS evidence (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    case_id VARCHAR(64) NOT NULL REFERENCES cases(id) ON DELETE CASCADE,
    description TEXT,
    discovered_at TIMESTAMP,
    location_found VARCHAR(255),
    collected_by VARCHAR(255),
    analyzed BOOLEAN DEFAULT FALSE,
    evidence_type VARCHAR(32) NOT NULL, -- 'DOCUMENT' or 'DIGITAL'
    author VARCHAR(255),
    page_count INT DEFAULT 0,
    classified BOOLEAN DEFAULT FALSE,
    document_type VARCHAR(255),
    device_type VARCHAR(255),
    file_hash VARCHAR(255),
    ip_address VARCHAR(64),
    file_size_bytes BIGINT DEFAULT 0,
    discovered_in_session BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_evidence_case ON evidence(case_id);
CREATE INDEX IF NOT EXISTS idx_evidence_type ON evidence(evidence_type);

-- 5. Case-Evidence Association (Module 3)
CREATE TABLE IF NOT EXISTS case_evidence (
    case_id VARCHAR(64) NOT NULL REFERENCES cases(id) ON DELETE CASCADE,
    evidence_id VARCHAR(64) NOT NULL REFERENCES evidence(id) ON DELETE CASCADE,
    PRIMARY KEY (case_id, evidence_id)
);

CREATE INDEX IF NOT EXISTS idx_case_evidence_case ON case_evidence(case_id);

-- 6. Clues Table (Module 3)
CREATE TABLE IF NOT EXISTS clues (
    id VARCHAR(64) PRIMARY KEY,
    case_id VARCHAR(64) NOT NULL REFERENCES cases(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    unlocked BOOLEAN DEFAULT FALSE,
    unlocked_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_clues_case ON clues(case_id);

-- 7. Case-Clues Association (Module 3)
CREATE TABLE IF NOT EXISTS case_clues (
    case_id VARCHAR(64) NOT NULL REFERENCES cases(id) ON DELETE CASCADE,
    clue_id VARCHAR(64) NOT NULL REFERENCES clues(id) ON DELETE CASCADE,
    PRIMARY KEY (case_id, clue_id)
);

CREATE INDEX IF NOT EXISTS idx_case_clues_case ON case_clues(case_id);

-- 8. Clue Prerequisites Table (Module 3)
CREATE TABLE IF NOT EXISTS clue_prerequisites (
    clue_id VARCHAR(64) NOT NULL REFERENCES clues(id) ON DELETE CASCADE,
    required_evidence_id VARCHAR(64) NOT NULL REFERENCES evidence(id) ON DELETE CASCADE,
    PRIMARY KEY (clue_id, required_evidence_id)
);

CREATE INDEX IF NOT EXISTS idx_clue_prereq_clue ON clue_prerequisites(clue_id);

-- 9. Evidence Archive Table (Module 3 / Member 3 integration)
CREATE TABLE IF NOT EXISTS evidence_archive (
    archive_id VARCHAR(64) PRIMARY KEY,
    evidence_id VARCHAR(64) NOT NULL,
    case_id VARCHAR(64),
    name VARCHAR(255),
    summary TEXT,
    archived_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
