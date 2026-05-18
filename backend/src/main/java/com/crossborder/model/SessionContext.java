package com.crossborder.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionContext {
    private String sessionId;
    private List<ChatMessage> messages;
    private LocalDateTime lastActiveTime;
    private Map<String, Object> metadata;

    public SessionContext(String sessionId) {
        this.sessionId = sessionId;
        this.messages = new ArrayList<>();
        this.lastActiveTime = LocalDateTime.now();
        this.metadata = new ConcurrentHashMap<>();
    }

    public void addMessage(ChatMessage msg) {
        messages.add(msg);
        lastActiveTime = LocalDateTime.now();
    }

    public String getSessionId() { return sessionId; }
    public List<ChatMessage> getMessages() { return messages; }
    public LocalDateTime getLastActiveTime() { return lastActiveTime; }
    public void setLastActiveTime(LocalDateTime lastActiveTime) { this.lastActiveTime = lastActiveTime; }
    public Map<String, Object> getMetadata() { return metadata; }

    public int getMessageCount() {
        return messages.size();
    }
}
