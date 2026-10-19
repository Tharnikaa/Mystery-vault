package com.mysteryvault.ui.swing.adapters;

import com.mysteryvault.model.Statement;
import com.mysteryvault.model.ExtractedStatementData;
import com.mysteryvault.service.ContradictionDetectorService;
import com.mysteryvault.service.StatementRegexExtractor;
import com.mysteryvault.ui.swing.ports.InterrogationViewPort;
import com.mysteryvault.ui.swing.viewmodel.ChatMessageViewModel;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Production presentation adapter connecting InterrogationViewPort to Member 2's Statement,
 * StatementRegexExtractor, and ContradictionDetectorService (Module 6 & 7).
 */
public class IntegratedInterrogationAdapter implements InterrogationViewPort {

    private final StatementRegexExtractor regexExtractor;
    private final ContradictionDetectorService contradictionDetector;
    private final Map<String, List<ChatMessageViewModel>> historyMap = new HashMap<>();

    public IntegratedInterrogationAdapter() {
        this(new StatementRegexExtractor(), new ContradictionDetectorService());
    }

    public IntegratedInterrogationAdapter(StatementRegexExtractor regexExtractor,
                                         ContradictionDetectorService contradictionDetector) {
        this.regexExtractor = Objects.requireNonNull(regexExtractor, "regexExtractor cannot be null");
        this.contradictionDetector = Objects.requireNonNull(contradictionDetector, "contradictionDetector cannot be null");

        initDefaultHistory();
    }

    private void initDefaultHistory() {
        List<ChatMessageViewModel> meeraChat = new ArrayList<>();
        meeraChat.add(new ChatMessageViewModel(true, "Detective", "Where were you at 8:45 PM during the gallery dinner?"));
        meeraChat.add(new ChatMessageViewModel(false, "Meera", "I was speaking with Arjun near the east salon. I never went anywhere near the sommelier pantry."));
        historyMap.put("MEERA", meeraChat);
        historyMap.put("SUS-103-2", meeraChat);

        List<ChatMessageViewModel> arjunChat = new ArrayList<>();
        arjunChat.add(new ChatMessageViewModel(true, "Detective", "Can you account for your whereabouts before the collapse?"));
        arjunChat.add(new ChatMessageViewModel(false, "Arjun", "I was reviewing security monitors in the office. Julian called me, but I didn't pick up."));
        historyMap.put("ARJUN", arjunChat);
        historyMap.put("SUS-103-1", arjunChat);

        List<ChatMessageViewModel> rohanChat = new ArrayList<>();
        rohanChat.add(new ChatMessageViewModel(true, "Detective", "Did anyone enter the pantry while the vintage wine was decanting?"));
        rohanChat.add(new ChatMessageViewModel(false, "Rohan", "The pantry was locked, but the service corridor door was left ajar around 8:40 PM."));
        historyMap.put("ROHAN", rohanChat);
        historyMap.put("SUS-103-3", rohanChat);
    }

    @Override
    public synchronized List<ChatMessageViewModel> getDialogueHistory(String suspectId) {
        String key = resolveKey(suspectId);
        return new ArrayList<>(historyMap.computeIfAbsent(key, k -> new ArrayList<>()));
    }

    @Override
    public synchronized ChatMessageViewModel askQuestion(String suspectId, String question) {
        String key = resolveKey(suspectId);
        List<ChatMessageViewModel> history = historyMap.computeIfAbsent(key, k -> new ArrayList<>());

        history.add(new ChatMessageViewModel(true, "Detective", question));

        // Use StatementRegexExtractor to analyze query parameters
        Statement detectiveStatement = new Statement("Q-" + System.currentTimeMillis(), "CASE-103", "Detective", question, LocalDateTime.now(), "INTERROGATION");
        ExtractedStatementData extracted = regexExtractor.extract(detectiveStatement);

        String replyText;
        String q = question.toLowerCase();

        if (q.contains("poison") || q.contains("alkaloid") || q.contains("glass") || q.contains("e01")) {
            replyText = "I had nothing to do with any poison! Check the sommelier inventory or the server logs.";
        } else if (q.contains("will") || q.contains("money") || q.contains("inheritance") || q.contains("audit")) {
            replyText = "Julian's financial decisions were his own. I had no motive to harm him!";
        } else if (!extracted.getTimes().isEmpty()) {
            replyText = "At " + extracted.getTimes().get(0).getParsedValue() + "? I have already stated my alibi to the officers.";
        } else if (q.contains("arjun") || q.contains("marcus")) {
            replyText = "We spoke briefly regarding company finances, but that was strictly professional.";
        } else if (q.contains("cellar") || q.contains("pantry") || q.contains("door")) {
            replyText = "I never went inside that cellar pantry. Anyone claiming they saw me there is mistaken.";
        } else {
            replyText = "I have told you everything I know regarding that evening. You are chasing shadows, detective.";
        }

        ChatMessageViewModel response = new ChatMessageViewModel(false, key, replyText);
        history.add(response);
        return response;
    }

    @Override
    public synchronized ChatMessageViewModel showEvidence(String suspectId, String evidenceId) {
        String key = resolveKey(suspectId);
        List<ChatMessageViewModel> history = historyMap.computeIfAbsent(key, k -> new ArrayList<>());

        history.add(new ChatMessageViewModel(true, "Detective", "[Presents Evidence: " + evidenceId + "] How do you explain this?"));

        String reply;
        String ev = evidenceId.toUpperCase();
        if (ev.contains("E01") || ev.contains("WINE") || ev.contains("EVD-103-1")) {
            reply = "That wine glass was used by dozens of people during the reception! Fingerprints prove nothing!";
        } else if (ev.contains("E03") || ev.contains("CCTV") || ev.contains("EVD-103-2")) {
            reply = "That camera angle is obscured! You cannot definitively prove that was me in the pantry corridor!";
        } else {
            reply = "I don't recognize that item, and it has no connection to me.";
        }

        ChatMessageViewModel resp = new ChatMessageViewModel(false, key, reply);
        history.add(resp);
        return resp;
    }

    @Override
    public synchronized ChatMessageViewModel challengeStatement(String suspectId, String contradictionId) {
        String key = resolveKey(suspectId);
        List<ChatMessageViewModel> history = historyMap.computeIfAbsent(key, k -> new ArrayList<>());

        history.add(new ChatMessageViewModel(true, "Detective", "Your statements contradict verified timeline logs! Care to clarify?"));

        String reply = "Wait... I might have been mistaken about the exact timestamp. The salon clock was running five minutes slow!";
        ChatMessageViewModel resp = new ChatMessageViewModel(false, key, reply);
        history.add(resp);
        return resp;
    }

    private String resolveKey(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "MEERA";
        String u = raw.trim().toUpperCase();
        if (u.contains("MEERA") || u.contains("ELENA") || u.contains("SUS-103-2")) return "MEERA";
        if (u.contains("ARJUN") || u.contains("MARCUS") || u.contains("SUS-103-1")) return "ARJUN";
        if (u.contains("ROHAN") || u.contains("PRICE") || u.contains("SUS-103-3")) return "ROHAN";
        return u;
    }
}
