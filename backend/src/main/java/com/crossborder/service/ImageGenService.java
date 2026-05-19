package com.crossborder.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class ImageGenService {

    private static final Logger log = LoggerFactory.getLogger(ImageGenService.class);
    private static final int MAX_POLL_SECONDS = 120;
    private static final int POLL_INTERVAL_MS = 2000;

    @Value("${dashscope.api-key}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(30))
        .build();

    public void generateImage(SseEmitter emitter, String prompt) {
        try {
            // Send progress: submitting task
            sendEvent(emitter, "progress", "正在提交图片生成任务...");

            // Step 1: Submit generation task
            String taskId = submitTask(prompt);
            log.info("Image task submitted: {}", taskId);

            sendEvent(emitter, "progress", "任务已提交，正在生成图片...");

            // Step 2: Poll for result
            String imageUrl = pollTask(taskId, emitter);

            if (imageUrl != null) {
                // Return the image URL
                ObjectNode resultNode = objectMapper.createObjectNode();
                resultNode.put("type", "image");
                resultNode.put("url", imageUrl);
                resultNode.put("prompt", prompt);

                sendEvent(emitter, "message", objectMapper.writeValueAsString(resultNode));
                sendEvent(emitter, "done", "[DONE]");
            } else {
                sendEvent(emitter, "error", "{\"type\":\"error\",\"content\":\"图片生成失败\"}");
                sendEvent(emitter, "done", "[DONE]");
            }

            emitter.complete();
        } catch (Exception e) {
            log.error("Image generation failed: {}", e.getMessage());
            sendEvent(emitter, "error", "{\"type\":\"error\",\"content\":\"生成图片时出错: " + escapeJson(e.getMessage()) + "\"}");
            sendEvent(emitter, "done", "[DONE]");
            try { emitter.complete(); } catch (Exception ignored) {}
        }
    }

    private String submitTask(String prompt) throws IOException, InterruptedException {
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", "z-image-turbo");

        ObjectNode input = objectMapper.createObjectNode();
        ArrayNode messages = objectMapper.createArrayNode();
        ObjectNode message = objectMapper.createObjectNode();
        message.put("role", "user");
        ArrayNode content = objectMapper.createArrayNode();
        ObjectNode textContent = objectMapper.createObjectNode();
        textContent.put("text", prompt);
        content.add(textContent);
        message.set("content", content);
        messages.add(message);
        input.set("messages", messages);
        requestBody.set("input", input);

        ObjectNode parameters = objectMapper.createObjectNode();
        parameters.put("prompt_extend", false);
        parameters.put("size", "1120*1440");
        requestBody.set("parameters", parameters);

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation"))
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode responseNode = objectMapper.readTree(response.body());

        // Check for task_id
        JsonNode output = responseNode.get("output");
        if (output != null && output.has("task_id")) {
            return output.get("task_id").asText();
        }

        // Check for direct result (some models return directly)
        if (output != null && output.has("results")) {
            return "DIRECT_RESULT";
        }

        log.error("Unexpected response: {}", response.body());
        throw new RuntimeException("Failed to submit image task: " + response.body());
    }

    private String pollTask(String taskId, SseEmitter emitter) throws IOException, InterruptedException {
        if ("DIRECT_RESULT".equals(taskId)) {
            return null;
        }

        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < MAX_POLL_SECONDS * 1000L) {
            Thread.sleep(POLL_INTERVAL_MS);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://dashscope.aliyuncs.com/api/v1/tasks/" + taskId))
                .header("Authorization", "Bearer " + apiKey)
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode responseNode = objectMapper.readTree(response.body());
            JsonNode output = responseNode.get("output");

            if (output == null) {
                log.warn("No output in poll response: {}", response.body());
                continue;
            }

            String status = output.has("task_status") ? output.get("task_status").asText() : "";

            switch (status) {
                case "SUCCEEDED":
                    if (output.has("results") && output.get("results").isArray()
                            && output.get("results").size() > 0) {
                        JsonNode result = output.get("results").get(0);
                        if (result.has("url")) {
                            return result.get("url").asText();
                        }
                        if (result.has("image") && result.get("image").has("url")) {
                            return result.get("image").get("url").asText();
                        }
                    }
                    log.warn("SUCCEEDED but no URL found: {}", response.body());
                    return null;

                case "FAILED":
                    String errorMsg = output.has("message") ? output.get("message").asText() : "未知错误";
                    log.error("Image task failed: {}", errorMsg);
                    return null;

                case "CANCELED":
                    log.warn("Image task canceled");
                    return null;

                case "PENDING":
                case "RUNNING":
                    // Still in progress, continue polling
                    int elapsed = (int) ((System.currentTimeMillis() - startTime) / 1000);
                    sendEvent(emitter, "progress",
                        "{\"type\":\"progress\",\"content\":\"正在生成图片... (" + elapsed + "s)\"}");
                    break;

                default:
                    log.warn("Unknown task status: {}", status);
            }
        }

        log.warn("Image task polling timed out for task: {}", taskId);
        return null;
    }

    private void sendEvent(SseEmitter emitter, String name, String data) {
        try {
            emitter.send(SseEmitter.event().name(name).data(data));
        } catch (IOException e) {
            log.warn("Failed to send SSE event: {}", e.getMessage());
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
