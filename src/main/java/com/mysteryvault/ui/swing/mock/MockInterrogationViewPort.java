package com.mysteryvault.ui.swing.mock;

import com.mysteryvault.ui.swing.ports.InterrogationViewPort;
import com.mysteryvault.ui.swing.viewmodel.ChatMessageViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockInterrogationViewPort implements InterrogationViewPort {

    private final Map<String, List<ChatMessageViewModel>> history = new HashMap<>();

    public MockInterrogationViewPort() {
        List<ChatMessageViewModel> meeraChat = new ArrayList<>();
        meeraChat.add(new ChatMessageViewModel(true, "Detective", "Where were you at 8:45 PM?"));
        meeraChat.add(new ChatMessageViewModel(false, "Meera", "I was speaking with Arjun near the balcony."));
        history.put("MEERA", meeraChat);
    }

    @Override
    public List<ChatMessageViewModel> getDialogueHistory(String suspectId) {
        String key = suspectId != null ? suspectId.toUpperCase() : "MEERA";
        return new ArrayList<>(history.computeIfAbsent(key, k -> new ArrayList<>()));
    }

    @Override
    public ChatMessageViewModel askQuestion(String suspectId, String question) {
        String reply;
        String q = question.toLowerCase();
        if (q.contains("poison") || q.contains("glass")) {
            reply = "I had nothing to do with any poison! Check the bartender's records.";
        } else if (q.contains("arjun")) {
            reply = "Arjun and I were discussing the quarterly accounts, that is all.";
        } else {
            reply = "I have already told you everything I know about tonight's events.";
        }
        return new ChatMessageViewModel(false, suspectId != null ? suspectId : "Suspect", reply);
    }

    @Override
    public ChatMessageViewModel showEvidence(String suspectId, String evidenceId) {
        return new ChatMessageViewModel(false, suspectId != null ? suspectId : "Suspect",
                "...I had no idea what was in his glass. Someone else poured it!");
    }

    @Override
    public ChatMessageViewModel challengeStatement(String suspectId, String contradictionId) {
        return new ChatMessageViewModel(false, suspectId != null ? suspectId : "Suspect",
                "Wait... I was only there for a few seconds to retrieve my purse!");
    }
}
