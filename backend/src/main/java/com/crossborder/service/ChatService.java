package com.crossborder.service;

import com.crossborder.model.ChatMessage;
import com.crossborder.model.SessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final SessionService sessionService;
    private final DashScopeService dashScopeService;

    public ChatService(SessionService sessionService, DashScopeService dashScopeService) {
        this.sessionService = sessionService;
        this.dashScopeService = dashScopeService;
    }

    public SseEmitter processMessage(String message, String sessionId) {
        SseEmitter emitter = new SseEmitter(300_000L);

        SessionContext ctx = sessionService.getOrCreateSession(sessionId);
        String actualSessionId = ctx.getSessionId();

        // Save user message to context
        ctx.addMessage(new ChatMessage("user", message));

        // Get system prompt and conversation history
        String systemPrompt = sessionService.getSystemPrompt();

        // Stream AI response and save to context on completion
        dashScopeService.streamChat(emitter, systemPrompt, ctx.getMessages(), message,
            fullResponse -> {
                // Save assistant response to session context for conversation memory
                ctx.addMessage(new ChatMessage("assistant", fullResponse));
                log.debug("Saved assistant response to session {}, length={}", actualSessionId, fullResponse.length());
            }
        );

        return emitter;
    }
}
