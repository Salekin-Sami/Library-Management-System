package com.library.chat;

import java.time.LocalDateTime;

public class ChatMessage {
    private String sender;
    private String content;
    private LocalDateTime timestamp;
    private String recipientId; // student email or ID

    public ChatMessage(String sender, String content, String recipientId) {
        this.sender = sender;
        this.content = content;
        this.recipientId = recipientId;
        this.timestamp = LocalDateTime.now();
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getRecipientId() {
        return recipientId;
    }

    @Override
    public String toString() {
        return sender + ": " + content;
    }
}
