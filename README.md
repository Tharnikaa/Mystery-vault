# MysteryVault: Uncover the Truth

**A Desktop Detective Mystery Game UI Built with Java Swing**

MysteryVault is a Java desktop detective game prototype that presents a cinematic investigation experience. Players navigate through detective profiles, case selection, suspects, evidence, interrogations, timelines, clue connections, case analysis, and final deduction screens.

The project focuses on a native desktop interface built with Java Swing, AWT, and Java2D, featuring a dark noir theme, gold accents, custom-drawn components, and screen-to-screen navigation.

## Overview

- **Platform:** Desktop application
- **Language:** Java 17+
- **UI Framework:** Java Swing
- **Graphics:** AWT and Java2D
- **Build System:** Apache Maven
- **Database Dependency:** SQLite JDBC
- **Application Entry Point:** `com.mysteryvault.MysteryVaultApplication`

## Features

### Cinematic Noir Interface

- Dark navy and charcoal backgrounds with gold accents.
- Custom-drawn buttons with hover and pressed states.
- Rounded cards, status badges, and progress indicators.
- Anti-aliased Java2D graphics and image scaling.
- Consistent styling across the investigation screens.

### Case Investigation Interface

The application contains 12 main screens representing the detective investigation workflow.

| # | Screen | Purpose |
|---|---|---|
| 1 | Main Menu | Access the main game menu and navigation options. |
| 2 | Detective Profile | Display detective rank, XP, and case statistics. |
| 3 | Case Selection | Browse four case cards and select an investigation. |
| 4 | Investigation Hub | Access suspects, evidence, timeline, clues, and analysis. |
| 5 | Suspect Screen | View suspect profiles, portraits, and suspicion indicators. |
| 6 | Evidence Investigation | Browse evidence and access examination actions. |
| 7 | Interrogation | View dialogue elements and suspect-interrogation controls. |
| 8 | Investigation Timeline | View chronological events and timeline controls. |
| 9 | Case Connection Engine | Explore a visual network of suspects and evidence. |
| 10 | Case Analysis | View analysis progress indicators and results. |
| 11 | Final Deduction | Select a suspected culprit, motive, and supporting evidence. |
| 12 | Case Result | View the case outcome and result-screen actions. |

### Case Cards

The case-selection interface displays four mystery scenarios:

1. **The Stolen Diamond**
2. **The Library Code**
3. **The Poisoned Drink**
4. **The Missing Prototype**

These are the scenarios represented in the current interface. Their appearance in the UI does not by itself imply that each case has a complete, independent investigation engine or persistent case database.

## Technology Stack

| Technology | Usage |
|---|---|
| Java 17+ | Core application language |
| Java Swing | Windows, panels, forms, and UI controls |
| AWT | Desktop UI infrastructure and event handling |
| Java2D | Custom graphics, drawing, and visual effects |
| SQLite JDBC | Database connectivity dependency |
| Apache Maven | Dependency management, compilation, and packaging |
| JUnit 5 | Test framework dependencies |

The current `pom.xml` configures Java 17 compilation and the Maven Shade Plugin for packaging.

## Prerequisites

Install the following software before running the project:

- **JDK 17 or newer**
- **Apache Maven 3.8 or newer** (if running Maven commands directly)
- **Windows PowerShell** for the provided PowerShell scripts

Verify your Java and Maven installations:

```powershell
java -version
javac -version
mvn -version
```

## Getting Started

### 1. Clone the repository

```powershell
git clone https://github.com/Tharnikaa/Mystery-vault.git
cd Mystery-vault
```

### 2. Build the project

Using Maven:

```powershell
mvn clean package
```

Alternatively, use the provided PowerShell build script:

```powershell
.\build.ps1
```

### 3. Run the application

Using the provided launcher:

```powershell
.\run.ps1
```

Or run the packaged JAR:

```powershell
java -jar target/mystery-vault-1.0.0.jar
```

The JAR must first be built if it is not already present.

**Note:** The repository's build and launch scripts should be used as provided. If a command fails, verify the JDK installation, Maven configuration, and the script's execution requirements.

## Navigation

The interface is designed to guide users through the following sequence:

**Main Menu → Detective Profile → Case Selection → Investigation Hub → Investigation Screens → Final Deduction → Case Result**

The interface also provides navigation controls and quick screen switching for demonstration and review.

Keyboard shortcuts documented by the current interface:

| Shortcut | Screen |
|---|---|
| `1`–`9` | Screens 1–9 |
| `0` | Case Analysis |
| `-` | Final Deduction |
| `=` | Case Result |

Shortcut availability depends on the application's current implementation and keyboard-focus handling.

## Project Structure

The repository uses a standard Maven project configuration.

```text
Mystery-vault/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── mysteryvault/
│       │           └── ...
│       └── resources/
│           └── ...
├── pom.xml
├── build.ps1
├── run.ps1
├── run.bat
├── .gitignore
└── README.md
```

The `src/main` directory contains the application source and resources. The `pom.xml` defines the Java version, dependencies, and build plugins.

## Current Project Scope

The current `main` branch is presented as a **desktop interface prototype**.

Its focus is the visual layout and navigation of the detective investigation experience. UI elements such as suspect selection, evidence examination, interrogation, clue connections, and final deduction should not automatically be interpreted as fully implemented game mechanics.

The following capabilities require separate verification before being described as complete:

- Independent case data and case-specific evidence.
- Functional suspect interrogation and contradiction detection.
- Persistent player profiles, XP, and case statistics.
- Prerequisite-based clue unlocking.
- Timeline conflict analysis.
- Database-backed deduction validation and scoring.
- Automated tests and verified test results.
- Exportable case reports and persistent save/resume functionality.

SQLite JDBC is included as a dependency in the Maven configuration; this alone does not establish that the complete game uses a working SQLite database.

## Future Development

Potential next steps for evolving the prototype into a complete detective game include:

- Implementing distinct storylines, suspects, and evidence for each case.
- Connecting interface actions to investigation logic.
- Adding persistent storage for player progress and case state.
- Implementing clue dependencies and timeline analysis.
- Validating final deductions against case-specific solutions.
- Adding automated tests for the investigation workflow.
- Verifying packaging and launch behavior on a clean installation.

## Repository

GitHub: https://github.com/Tharnikaa/Mystery-vault

---

*Built with Java Swing, AWT, and Java2D.*
