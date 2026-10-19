package com.mysteryvault.ui.swing.ports;

import com.mysteryvault.ui.swing.viewmodel.ChatMessageViewModel;
import java.util.List;

/**
 * Presentation port for suspect dialogue and statement confrontation.
 * Future integration: Member 2 (Statement/Regex / Module 6) & agreed dialogue provider.
 */
public interface InterrogationViewPort {
    List<ChatMessageViewModel> getDialogueHistory(String suspectId);
    ChatMessageViewModel askQuestion(String suspectId, String question);
    ChatMessageViewModel showEvidence(String suspectId, String evidenceId);
    ChatMessageViewModel challengeStatement(String suspectId, String contradictionId);
}
