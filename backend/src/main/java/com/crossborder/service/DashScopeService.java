package com.crossborder.service;

import com.crossborder.model.ChatMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

@Service
public class DashScopeService {

    private static final Logger log = LoggerFactory.getLogger(DashScopeService.class);

    @Value("${dashscope.api-key}")
    private String apiKey;

    @Value("${dashscope.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}")
    private String baseUrl;

    @Value("${dashscope.model:qwen-plus}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(java.time.Duration.ofSeconds(30))
        .build();

    /**
     * Stream chat response from DashScope.
     *
     * @param emitter        SSE emitter for streaming to frontend
     * @param systemPrompt   the system prompt
     * @param history        conversation history
     * @param userMessage    the current user message
     * @param onComplete     callback with the full assistant response text (may be null)
     */
    public void streamChat(SseEmitter emitter, String systemPrompt, List<ChatMessage> history,
                           String userMessage, Consumer<String> onComplete) {
        try {
            ArrayNode messagesArray = objectMapper.createArrayNode();

            ObjectNode systemNode = objectMapper.createObjectNode();
            systemNode.put("role", "system");
            systemNode.put("content", systemPrompt);
            messagesArray.add(systemNode);

            for (ChatMessage msg : history) {
                ObjectNode historyNode = objectMapper.createObjectNode();
                String role = "user".equals(msg.getRole()) ? "user" : "assistant";
                historyNode.put("role", role);
                historyNode.put("content", msg.getContent());
                messagesArray.add(historyNode);
            }

            ObjectNode userNode = objectMapper.createObjectNode();
            userNode.put("role", "user");
            userNode.put("content", userMessage);
            messagesArray.add(userNode);

            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);
            requestBody.set("messages", messagesArray);
            requestBody.put("stream", true);
            requestBody.put("enable_search", true);

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/chat/completions"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("Accept", "text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .thenAccept(response -> {
                    StringBuilder fullResponse = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("data: ")) {
                                String data = line.substring(6).trim();
                                if ("[DONE]".equals(data)) {
                                    continue;
                                }
                                try {
                                    JsonNode dataNode = objectMapper.readTree(data);
                                    JsonNode choices = dataNode.get("choices");
                                    if (choices != null && choices.isArray() && choices.size() > 0) {
                                        JsonNode delta = choices.get(0).get("delta");
                                        if (delta != null) {
                                            JsonNode content = delta.get("content");
                                            if (content != null && !content.asText().isEmpty()) {
                                                String text = content.asText();
                                                fullResponse.append(text);
                                                SseEmitter.SseEventBuilder event = SseEmitter.event()
                                                    .name("message")
                                                    .data("{\"type\":\"text\",\"content\":\"" + escapeJson(text) + "\"}")
                                                    .id(String.valueOf(System.nanoTime()));
                                                emitter.send(event);
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    log.warn("Failed to parse DashScope response: {}", e.getMessage());
                                }
                            }
                        }
                    } catch (IOException e) {
                        log.error("Error reading DashScope stream: {}", e.getMessage());
                    } finally {
                        // Notify completion with full text
                        if (onComplete != null && fullResponse.length() > 0) {
                            onComplete.accept(fullResponse.toString());
                        }
                        try {
                            emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                            emitter.complete();
                        } catch (IOException e) {
                            log.warn("Failed to send done event: {}", e.getMessage());
                        }
                    }
                })
                .exceptionally(throwable -> {
                    log.error("DashScope API call failed: {}", throwable.getMessage());
                    try {
                        emitter.send(SseEmitter.event()
                            .name("error")
                            .data("{\"type\":\"error\",\"content\":\"AI 服务调用失败，请稍后重试\"}"));
                        emitter.complete();
                    } catch (IOException e) {
                        log.error("Failed to send error event: {}", e.getMessage());
                    }
                    return null;
                });

        } catch (Exception e) {
            log.error("Failed to start DashScope stream: {}", e.getMessage());
            try {
                emitter.send(SseEmitter.event()
                    .name("error")
                    .data("{\"type\":\"error\",\"content\":\"请求处理失败: " + escapeJson(e.getMessage()) + "\"}"));
            } catch (IOException ex) {
                log.error("Failed to send error to emitter: {}", ex.getMessage());
            }
            emitter.complete();
        }
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }
}
