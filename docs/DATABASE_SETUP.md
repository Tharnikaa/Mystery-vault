# Mystery Vault — Database Configuration & Setup Guide

## 1. Configured Database System
Mystery Vault is configured for **PostgreSQL 14+** in production, with ANSI-standard SQL compliance and automated schema initialization.

### Database Connection Parameters
Mystery Vault reads its configuration from system environment variables or Java system properties:

| Environment Variable | Default Value | Description |
|----------------------|---------------|-------------|
| `MYSTERYVAULT_DB_URL` | `jdbc:postgresql://localhost:5432/mysteryvault` | JDBC connection string |
| `MYSTERYVAULT_DB_USERNAME` | `postgres` | Database superuser/application user |
| `MYSTERYVAULT_DB_PASSWORD` | *(empty string)* | Database user password |

---

## 2. Setting Up Local PostgreSQL

### A. Create the Database
Ensure PostgreSQL service is running, then execute via `psql` or pgAdmin:

```sql
CREATE DATABASE mysteryvault;
```

### B. Configure Environment Variables
#### Windows (PowerShell):
```powershell
$env:MYSTERYVAULT_DB_URL = "jdbc:postgresql://localhost:5432/mysteryvault"
$env:MYSTERYVAULT_DB_USERNAME = "postgres"
$env:MYSTERYVAULT_DB_PASSWORD = "your_actual_password_here"
```

#### Windows (Command Prompt):
```cmd
set MYSTERYVAULT_DB_URL=jdbc:postgresql://localhost:5432/mysteryvault
set MYSTERYVAULT_DB_USERNAME=postgres
set MYSTERYVAULT_DB_PASSWORD=your_actual_password_here
```

#### Linux / macOS:
```bash
export MYSTERYVAULT_DB_URL="jdbc:postgresql://localhost:5432/mysteryvault"
export MYSTERYVAULT_DB_USERNAME="postgres"
export MYSTERYVAULT_DB_PASSWORD="your_actual_password_here"
```

---

## 3. Schema Migrations & Initialization
Database schema DDL is located at:
`src/main/resources/schema.sql`

Schema initialization is fully automated in code via:
```java
DatabaseConnectionFactory factory = new DatabaseConnectionFactory(DatabaseConfig.fromEnvironment());
factory.initializeSchema();
```
The script is idempotent using `IF NOT EXISTS` constructs across all tables and indexes.

### Managed Database Tables
1. **`cases`**: Master investigation case dossiers, statuses, priorities, timestamps.
2. **`suspects`**: Suspect and person profiles, occupations, alibis, suspicion scores (0–100), verified flags.
3. **`case_suspects`**: Many-to-many relationship scoping suspects to individual cases.
4. **`evidence`**: Polymorphic document and digital evidence storage with hashes, paths, authors, and classification.
5. **`case_evidence`**: Relational junction table linking evidence to cases.
6. **`clues`**: Investigation deductions and unlocked states.
7. **`case_clues`**: Relational junction table linking clues to cases.
8. **`clue_prerequisites`**: Prerequisite evidence IDs required to unlock each clue.
9. **`evidence_archive`**: Archival storage for long-term preserved evidence records.

---

## 4. Running Tests & Demo
### Quick Run (Windows)
```cmd
run.bat
```
Or execute PowerShell:
```powershell
.\build.ps1
```

### Running Against PostgreSQL
When PostgreSQL is running and environment variables are set:
1. `CaseService.createProductionService()` connects to live PostgreSQL.
2. `PostgreSqlIntegrationTest` executes live queries against PostgreSQL.
3. In-memory test doubles remain available for rapid, isolated unit testing without requiring an active PostgreSQL daemon.
