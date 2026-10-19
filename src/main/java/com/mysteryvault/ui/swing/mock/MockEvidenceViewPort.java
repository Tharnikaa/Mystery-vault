package com.mysteryvault.ui.swing.mock;

import com.mysteryvault.ui.swing.ports.EvidenceViewPort;
import com.mysteryvault.ui.swing.viewmodel.EvidenceViewModel;

import java.util.ArrayList;
import java.util.List;

public class MockEvidenceViewPort implements EvidenceViewPort {

    private final List<EvidenceViewModel> evidenceList = new ArrayList<>();

    public MockEvidenceViewPort() {
        evidenceList.add(new EvidenceViewModel(
                "E01", "E01 – Wine Glass", "Physical Evidence", "Restaurant VIP Table", "09:45 PM",
                "Traces of an unidentified toxic alkaloid detected on the rim of the crystal glass.",
                "evidence_wineglass.png"
        ));
        evidenceList.add(new EvidenceViewModel(
                "E02", "E02 – Receipt", "Documentary Evidence", "Victim's Wallet", "08:15 PM",
                "Itemized dinner receipt confirming three cocktails and a high-value reservation deposit.",
                "case_diamond.png"
        ));
        evidenceList.add(new EvidenceViewModel(
                "E03", "E03 – CCTV Footage", "Digital Evidence", "Restaurant Security Cam 3", "08:32 PM",
                "Video showing Arjun standing near the lounge bar glancing repeatedly towards the victim's booth.",
                "case_prototype.png"
        ));
        evidenceList.add(new EvidenceViewModel(
                "E04", "E04 – Phone Log", "Telecommunications", "Victim's Smartphone", "09:10 PM",
                "Incoming encrypted call lasting 47 seconds from an untraceable burner telephone.",
                "case_library.png"
        ));
        evidenceList.add(new EvidenceViewModel(
                "E05", "E05 – Lab Report", "Forensic Analysis", "City Forensic Lab", "10:30 PM",
                "Toxicology confirms rapid-acting neurotoxin matching laboratory-grade synthetic digitalis.",
                "case_poison.png"
        ));
    }

    @Override
    public List<EvidenceViewModel> getEvidenceForCase(String caseId) {
        return new ArrayList<>(evidenceList);
    }

    @Override
    public EvidenceViewModel getEvidenceById(String evidenceId) {
        for (EvidenceViewModel e : evidenceList) {
            if (e.getId().equalsIgnoreCase(evidenceId)) return e;
        }
        return evidenceList.get(0);
    }

    @Override
    public String examineEvidence(String evidenceId) {
        return "Microscopic Examination: Traces of digitalis poison confirmed along with faint thumbprints on the stem.";
    }

    @Override
    public String sendToLab(String evidenceId) {
        return "Sample sent to Toxicology Department. Results match Case #103 toxin profile.";
    }
}
