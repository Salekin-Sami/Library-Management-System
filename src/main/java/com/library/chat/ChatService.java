package com.library.chat;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ChatService {
    private static final ObservableList<ChatMessage> messages = FXCollections.observableArrayList();

    private ChatService() {
    }

    public static ObservableList<ChatMessage> getMessages() {
        return messages;
    }

    public static ObservableList<ChatMessage> getMessagesForRecipient(String recipientId) {
        ObservableList<ChatMessage> filtered = FXCollections.observableArrayList();
        for (ChatMessage msg : messages) {
            if (recipientId != null && recipientId.equals(msg.getRecipientId())) {
                filtered.add(msg);
            }
        }
        return filtered;
    }

    public static void sendMessage(ChatMessage message) {
        messages.add(message);
    }
}
