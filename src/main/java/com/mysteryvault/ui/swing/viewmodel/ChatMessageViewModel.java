package com.mysteryvault.ui.swing.viewmodel;

public class ChatMessageViewModel {
    private final boolean isDetective;
    private final String sender;
    private final String message;

    public ChatMessageViewModel(boolean isDetective, String sender, String message) {
        this.isDetective = isDetective;
        this.sender = sender;
        this.message = message;
    }

    public boolean isDetective() { return isDetective; }
    public String getSender() { return sender; }
    public String getMessage() { return message; }
}
