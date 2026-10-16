# Mystery Vault – Uncover the Truth
**Detective Mystery Desktop Game UI in Java Swing, AWT, and Java 2D**

---

## 🔍 Overview
**Mystery Vault – Uncover the Truth** is a cinematic desktop mystery detective game UI created using **Java Swing, AWT, and Java 2D**.

The entire UI has been crafted to faithfully recreate all **12 screens** from the reference specification, maintaining the dark navy, charcoal, and gold aesthetic, glassmorphic card containers, custom gradients, typography, and interactive navigation.

---

## 🎨 Aesthetic & Design System
- **Cinematic Noir Palette:**
  - Base Background: Deep dark slate / charcoal navy (`#0d111a`, `#090c13`)
  - Glassmorphic Cards: Translucent navy cards (`#131926`, `rgba(19, 25, 38, 0.92)`)
  - Golden Amber Accents: `#d4a359`, `#f0c47d`, `#a57834`
  - Cyan / Teal Highlights: `#19c3b8`, glowing status dots and completed milestones
  - Crimson / Warning Accents: `#e14b4b`, conflict alerts and timeline warnings
- **Java 2D Graphics:**
  - Custom anti-aliased geometry, subpixel text rendering, and high-quality image bicubic scaling
  - Custom painted rounded buttons with interactive hover and press states
  - Reusable progress meters (`StatBar`), status badges (`Badge`), and headers (`ScreenHeader`)

---

## 🖥️ The 12 Screens Implemented

| # | Screen Name | Class Name | Description |
|---|---|---|---|
| **1** | **Main Menu** | [`MainMenuScreen`](src/main/java/com/mysteryvault/screens/MainMenuScreen.java) | Atmospheric detective desk artwork, title logo, subtitle, `NEW GAME`, `LOAD GAME`, `SETTINGS`, and `QUIT` buttons. |
| **2** | **Detective Profile** | [`DetectiveProfileScreen`](src/main/java/com/mysteryvault/screens/DetectiveProfileScreen.java) | Circular framed noir detective avatar, Rank, 37 XP progress bar, 4-stat cards (Cases Solved, Accuracy, Cases Attempted, Best Score), and `CONTINUE` button. |
| **3** | **Case Selection** | [`CaseSelectionScreen`](src/main/java/com/mysteryvault/screens/CaseSelectionScreen.java) | 2x2 grid of case cards (The Stolen Diamond, The Library Code, The Poisoned Drink [selected], The Missing Prototype) with status badges and `START INVESTIGATION` button. |
| **4** | **Main Investigation Hub** | [`InvestigationHubScreen`](src/main/java/com/mysteryvault/screens/InvestigationHubScreen.java) | Case title & subtitle, 8 action tiles (`SUSPECTS`, `EVIDENCE`, `TIMELINE`, `INTERROGATE`, `SEARCH`, `CLUES`, `CONNECTIONS`, `ANALYZE CASE`), and bottom progress tracker. |
| **5** | **Suspect Screen** | [`SuspectScreen`](src/main/java/com/mysteryvault/screens/SuspectScreen.java) | 3 suspect cards (Arjun, Meera [highlighted], Rohan) with portraits, suspicion meters, dossier information, and `VIEW EVIDENCE` / `INTERROGATE` actions. |
| **6** | **Evidence Investigation** | [`EvidenceScreen`](src/main/java/com/mysteryvault/screens/EvidenceScreen.java) | Split view: evidence items list (E01 to E05), high-res inspection photo, crime scene metadata, and `EXAMINE`, `SEND TO LAB`, `LINK TO SUSPECT` buttons. |
| **7** | **Interrogation** | [`InterrogationScreen`](src/main/java/com/mysteryvault/screens/InterrogationScreen.java) | Left suspect vitals panel (Trust 45%, Suspicion 72%), right dialogue log with detective & suspect speech bubbles, question input, and `SHOW EVIDENCE` / `CHALLENGE STATEMENT` / `EXIT`. |
| **8** | **Investigation Timeline** | [`TimelineScreen`](src/main/java/com/mysteryvault/screens/TimelineScreen.java) | Vertical glowing timeline with timestamps, events, warning box (`TIMELINE CONFLICT DETECTED`), `ADD EVENT`, `EDIT`, and `ANALYZE TIMELINE`. |
| **9** | **Case Connection Engine** | [`ConnectionEngineScreen`](src/main/java/com/mysteryvault/screens/ConnectionEngineScreen.java) | Interactive node network graph connecting Victim to suspects and evidence with glowing links, right sidebar for Connections Found, and `RUN ANALYSIS`. |
| **10** | **Case Analysis** | [`CaseAnalysisScreen`](src/main/java/com/mysteryvault/screens/CaseAnalysisScreen.java) | Analysis completion meters (Timeline, Evidence, Statement, Connection 90%), Results breakdown card, and `VIEW RESULTS` button. |
| **11** | **Final Deduction** | [`FinalDeductionScreen`](src/main/java/com/mysteryvault/screens/FinalDeductionScreen.java) | "WHO IS RESPONSIBLE?" form with radio selection for culprit, motive dropdown, supporting evidence checkboxes, and `SUBMIT DEDUCTION`. |
| **12** | **Case Result** | [`CaseResultScreen`](src/main/java/com/mysteryvault/screens/CaseResultScreen.java) | Victory card with gold trophy, "CASE SOLVED", 92/100 score badge, accuracy stats, Master Detective rank, and `NEXT CASE`, `CASE REPORT`, `RETURN TO MENU`. |

---

## 🚀 How to Run the Application

### Option 1: Using the provided PowerShell scripts
```powershell
# Compile the project and copy assets
.\build.ps1

# Run the desktop application
.\run.ps1
```

### Option 2: Using the pre-built JAR
```powershell
java -jar target/mystery-vault-1.0.0.jar
```

### Option 3: Using standard Maven (if installed)
```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="com.mysteryvault.Main"
```

---

## 🎮 Navigation Controls
- **In-Game Flow:** Every button in the game logically moves between screens (e.g., Main Menu -> Profile -> Case Selection -> Hub -> Suspects -> Evidence -> Interrogation -> Timeline -> Connection Engine -> Analysis -> Final Deduction -> Result -> Menu).
- **Back Button (`<`):** Navigates back in history on all sub-screens.
- **Quick Jump Switcher:** A dropdown bar at the bottom lets you jump directly to any of the 12 screens instantly for evaluation.
- **Keyboard Hotkeys:**
  - `1` through `9`: Screens 1 to 9
  - `0`: Screen 10 (Case Analysis)
  - `-`: Screen 11 (Final Deduction)
  - `=`: Screen 12 (Case Result)
