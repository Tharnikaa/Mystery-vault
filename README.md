# Mystery Vault — Uncover the Truth
**Detective Mystery Desktop Game UI & Team Integration Architecture**

---

## 🔍 Overview
**Mystery Vault – Uncover the Truth** is a cinematic desktop mystery detective game built with **Java Swing, AWT, and Java 2D** (Java 17+).

This repository contains:
1. **Module 13 (Application Shell / Complete UI):** All 12 game screens faithfully recreated according to the design reference specification.
2. **Module 4 (Clue Network Graph):** Dedicated Java 2D graph renderer (`CustomGraphCanvas`) and pure data model (`Connection`).
3. **Module 11 (Final Deduction / Case Solver):** Standalone deduction solver engine (`CaseSolverEngine`) and evaluation DTOs.
4. **Hexagonal Integration Seams:** Interface-driven presentation ports (`ui.swing.ports.*`) and deterministic mock adapters (`ui.swing.mock.*`) enabling independent frontend operation prior to merging backend modules from Members 1, 2, and 3.

---

## 🎨 Aesthetic & Design System
- **Cinematic Noir Palette:**
  - Base Background: Deep dark slate / charcoal navy (`#0d111a`, `#090c13`)
  - Glassmorphic Cards: Translucent navy cards (`#131926`, `rgba(19, 25, 38, 0.92)`)
  - Golden Amber Accents: `#d4a359`, `#f0c47d`, `#a57834`
  - Cyan / Teal Highlights: `#19c3b8`, glowing status dots and completed milestones
  - Crimson / Warning Accents: `#e14b4b`, conflict alerts and timeline warnings
- **Java 2D Graphics:**
  - Subpixel anti-aliasing (`RenderingHints.KEY_TEXT_ANTIALIAS_ON` and `KEY_ANTIALIAS_ON`)
  - Custom rounded buttons with interactive hover/press states (`CustomButton`)
  - Interactive node network visualization (`CustomGraphCanvas`)

---

## 🖥️ The 12 Screen Panels

| # | Screen Name | Panel Class | Associated Port |
|---|---|---|---|
| **1** | **Main Menu** | `MainMenuPanel` | *Shell / Save-Load* |
| **2** | **Detective Profile** | `DetectiveProfilePanel` | `ProfileViewPort` |
| **3** | **Case Selection** | `CaseSelectionPanel` | `CaseViewPort` |
| **4** | **Main Investigation Hub** | `InvestigationHubPanel` | `CaseViewPort` |
| **5** | **Suspect Screen** | `SuspectPanel` | `SuspectViewPort` |
| **6** | **Evidence Investigation** | `EvidencePanel` | `EvidenceViewPort` |
| **7** | **Interrogation** | `InterrogationPanel` | `InterrogationViewPort`, `SuspectViewPort` |
| **8** | **Investigation Timeline** | `TimelinePanel` | `TimelineViewPort` |
| **9** | **Case Connection Engine** | `ConnectionPanel` | `ConnectionViewPort` |
| **10** | **Case Analysis** | `AnalysisPanel` | `AnalysisViewPort` |
| **11** | **Final Deduction** | `DeductionPanel` | `DeductionViewPort` |
| **12** | **Case Result** | `ResultPanel` | `DeductionViewPort`, `ReportViewPort` |

---

## 🚀 Running the Application

### Option 1: Using PowerShell scripts
```powershell
# Compile main sources & tests, run unit tests, and package JAR
.\build.ps1

# Launch the desktop application
.\run.ps1
```

### Option 2: Using the pre-built JAR
```powershell
java -jar target/mystery-vault-1.0.0.jar
```

### Option 3: Double-click Batch File
```cmd
run.bat
```

---

## 🎮 Navigation & Evaluation Controls
- **In-Game Flow:** Buttons guide players logically through the mystery investigation.
- **Back Button (`<`):** Navigates back in history on all sub-screens.
- **Quick Jump Switcher:** A dropdown bar at the bottom allows instantaneous evaluation of any of the 12 screens.
- **Keyboard Hotkeys:**
  - `1` through `9`: Screens 1 to 9
  - `0`: Screen 10 (Case Analysis)
  - `-`: Screen 11 (Final Deduction)
  - `=`: Screen 12 (Case Result)
  - `Ctrl + N`: Open New Case Dialog

---

## 📚 Team Integration Documentation
- **[FRONTEND_INTEGRATION_GUIDE.md](FRONTEND_INTEGRATION_GUIDE.md):** Detailed guide on connecting Members 1, 2, and 3 services.
- **[INTEGRATION_NOTES.md](INTEGRATION_NOTES.md):** Implemented modules, public API contracts, and known limitations.
- **[docs/API_CONTRACT.md](docs/API_CONTRACT.md):** Team API contract comparison and Pending Team Approval items.
