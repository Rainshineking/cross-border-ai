package com.crossborder.controller;

import com.crossborder.model.ChatRequest;
import com.crossborder.service.ChatService;
import com.crossborder.service.SessionService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatService chatService;
    private final SessionService sessionService;

    public ChatController(ChatService chatService, SessionService sessionService) {
        this.chatService = chatService;
        this.sessionService = sessionService;
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody ChatRequest request) {
        String message = request.message();
        String sessionId = request.sessionId();
        return chatService.processMessage(message, sessionId);
    }

    @GetMapping("/session")
    public Map<String, String> createSession() {
        String sessionId = sessionService.createSession();
        return Map.of("sessionId", sessionId);
    }
}
