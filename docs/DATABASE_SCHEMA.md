# MysteryVault — Database Schema Specification

All databases are powered by SQLite 3, accessed exclusively via Java JDBC (`org.sqlite.JDBC`). Foreign keys are enforced on every connection (`PRAGMA foreign_keys = ON;`).

---

## 1. Global Catalog Database (`data/mysteryvault_catalog.db`)

Manages available cases, high-level metadata, player detective profiles, and saved investigation references.

### Table: `schema_version`
| Column | Type | Constraints | Description |
|---|---|---|---|
| `version` | INTEGER | PRIMARY KEY | Schema migration version |
| `applied_at` | TEXT | NOT NULL | ISO-8601 timestamp |

### Table: `cases_catalog`
| Column | Type | Constraints | Description |
|---|---|---|---|
| `case_id` | TEXT | PRIMARY KEY | Unique identifier (e.g. `CASE_101`) |
| `title` | TEXT | NOT NULL | Display title of the case |
| `description` | TEXT | NOT NULL | Synopsis of the mystery |
| `difficulty` | TEXT | NOT NULL | EASY, MEDIUM, HARD |
| `db_filename` | TEXT | NOT NULL | Relative path to DB (`case_101.db`) |
| `is_unlocked` | INTEGER | NOT NULL DEFAULT 1 | 1 if playable, 0 if locked |
| `is_completed` | INTEGER | NOT NULL DEFAULT 0 | 1 if solved |

### Table: `player_profile`
| Column | Type | Constraints | Description |
|---|---|---|---|
| `profile_id` | TEXT | PRIMARY KEY | Unique player ID (`default_detective`) |
| `detective_name` | TEXT | NOT NULL | Player display name |
| `experience_points` | INTEGER | NOT NULL DEFAULT 0 | Cumulative XP earned |
| `completed_cases` | INTEGER | NOT NULL DEFAULT 0 | Total solved cases |
| `correct_deductions` | INTEGER | NOT NULL DEFAULT 0 | Correct accusations |
| `total_deductions` | INTEGER | NOT NULL DEFAULT 0 | Total submitted accusations |
| `best_score` | INTEGER | NOT NULL DEFAULT 0 | Highest investigation score |

### Table: `saved_investigations`
| Column | Type | Constraints | Description |
|---|---|---|---|
| `investigation_id` | TEXT | PRIMARY KEY | Unique save ID |
| `case_id` | TEXT | NOT NULL REFERENCES `cases_catalog`(`case_id`) | Case referenced |
| `saved_at` | TEXT | NOT NULL | ISO-8601 timestamp |
| `progress_percent` | INTEGER | NOT NULL DEFAULT 0 | 0 to 100% |
| `notes_summary` | TEXT | | Brief note snippet |

---

## 2. Independent Case Database (`data/cases/case_{ID}.db`)

Each mystery case has its own completely independent SQLite database file.

### Schema Versioning & Safety
- Each case DB contains `schema_version(version INTEGER PRIMARY KEY, applied_at TEXT)`.
- Initialized programmatically via Java JDBC. Never recreated on application restart.

### Core Tables

#### `case_metadata`
- `case_id` TEXT PRIMARY KEY
- `title` TEXT NOT NULL
- `summary` TEXT NOT NULL
- `status` TEXT NOT NULL DEFAULT 'OPEN'

#### `canonical_solution` *(Restricted internal table)*
- `id` INTEGER PRIMARY KEY AUTOINCREMENT
- `culprit_id` TEXT NOT NULL
- `culprit_name` TEXT NOT NULL
- `motive` TEXT NOT NULL
- `decisive_evidence_ids` TEXT NOT NULL  -- Comma-separated
- `explanation` TEXT NOT NULL

#### `suspects`
- `suspect_id` TEXT PRIMARY KEY
- `name` TEXT NOT NULL
- `role` TEXT NOT NULL
- `occupation` TEXT NOT NULL
- `age` INTEGER NOT NULL
- `alibi_statement` TEXT NOT NULL
- `suspicion_level` INTEGER NOT NULL DEFAULT 20
- `is_prime_suspect` INTEGER NOT NULL DEFAULT 0

#### `evidence`
- `evidence_id` TEXT PRIMARY KEY
- `name` TEXT NOT NULL
- `type` TEXT NOT NULL  -- DIGITAL, PHYSICAL, DOCUMENT, FORENSIC
- `description` TEXT NOT NULL
- `location_found` TEXT NOT NULL
- `time_recorded` TEXT NOT NULL
- `associated_person_id` TEXT
- `significance` TEXT NOT NULL
- `is_discovered` INTEGER NOT NULL DEFAULT 0

#### `clues`
- `clue_id` TEXT PRIMARY KEY
- `title` TEXT NOT NULL
- `description` TEXT NOT NULL
- `status` TEXT NOT NULL DEFAULT 'LOCKED'  -- LOCKED, UNLOCKED, ANALYZED
- `prerequisite_evidence_ids` TEXT NOT NULL  -- Comma-separated

#### `timeline_events`
- `event_id` TEXT PRIMARY KEY
- `timestamp_str` TEXT NOT NULL  -- e.g. "08:15 PM"
- `sort_order` INTEGER NOT NULL
- `description` TEXT NOT NULL
- `person_id` TEXT
- `location` TEXT NOT NULL
- `is_verified` INTEGER NOT NULL DEFAULT 1
- `has_conflict` INTEGER NOT NULL DEFAULT 0
- `conflict_details` TEXT

#### `interrogation_dialogues`
- `dialogue_id` TEXT PRIMARY KEY
- `suspect_id` TEXT NOT NULL
- `question_text` TEXT NOT NULL
- `response_text` TEXT NOT NULL
- `is_asked` INTEGER NOT NULL DEFAULT 0
- `extracted_statement_id` TEXT

#### `statements`
- `statement_id` TEXT PRIMARY KEY
- `person_id` TEXT NOT NULL
- `statement_text` TEXT NOT NULL
- `time_recorded` TEXT NOT NULL
- `extracted_times` TEXT
- `extracted_locations` TEXT

#### `contradictions`
- `contradiction_id` TEXT PRIMARY KEY
- `title` TEXT NOT NULL
- `description` TEXT NOT NULL
- `suspect_id` TEXT NOT NULL
- `evidence_id` TEXT
- `timeline_event_id` TEXT
- `severity` TEXT NOT NULL  -- LOW, MEDIUM, HIGH, CRITICAL
- `is_resolved` INTEGER NOT NULL DEFAULT 0

#### `connections`
- `connection_id` TEXT PRIMARY KEY
- `source_id` TEXT NOT NULL
- `source_label` TEXT NOT NULL
- `target_id` TEXT NOT NULL
- `target_label` TEXT NOT NULL
- `relationship_type` TEXT NOT NULL  -- ACCUSES, OWNS, ALIBI_WITH, ASSOCIATED_WITH, CONTRADICTS
- `weight` REAL NOT NULL DEFAULT 1.0

#### `investigation_state`
- `key` TEXT PRIMARY KEY
- `value` TEXT NOT NULL
- `updated_at` TEXT NOT NULL
