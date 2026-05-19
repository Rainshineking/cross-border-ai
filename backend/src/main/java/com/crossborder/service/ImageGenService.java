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
            sendEvent(emitter, "progress", "正在提交图片生成任务...");

            // Submit and get result
            JsonNode responseNode = submitTask(prompt);

            // Try to extract image URL from response
            String imageUrl = extractImageUrl(responseNode);

            if (imageUrl != null) {
                // Got URL directly — no polling needed
                sendEvent(emitter, "progress", "图片生成完成");
                ObjectNode resultNode = objectMapper.createObjectNode();
                resultNode.put("type", "image");
                resultNode.put("url", imageUrl);
                resultNode.put("prompt", prompt);
                sendEvent(emitter, "message", objectMapper.writeValueAsString(resultNode));
                sendEvent(emitter, "done", "[DONE]");
                emitter.complete();
                return;
            }

            // Check for task_id — need to poll
            String taskId = extractTaskId(responseNode);
            if (taskId == null) {
                sendError(emitter, "无法解析图片生成结果");
                return;
            }

            sendEvent(emitter, "progress", "任务已提交，正在生成图片...");
            imageUrl = pollTask(taskId, emitter);

            if (imageUrl != null) {
                ObjectNode resultNode = objectMapper.createObjectNode();
                resultNode.put("type", "image");
                resultNode.put("url", imageUrl);
                resultNode.put("prompt", prompt);
                sendEvent(emitter, "message", objectMapper.writeValueAsString(resultNode));
                sendEvent(emitter, "done", "[DONE]");
            } else {
                sendError(emitter, "图片生成失败或超时");
            }
            emitter.complete();

        } catch (Exception e) {
            log.error("Image generation failed: {}", e.getMessage());
            sendError(emitter, "生成图片时出错: " + e.getMessage());
            try { emitter.complete(); } catch (Exception ignored) {}
        }
    }

    /** Submit image generation task to DashScope */
    private JsonNode submitTask(String prompt) throws IOException, InterruptedException {
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
        return objectMapper.readTree(response.body());
    }

    /** Extract image URL from direct response (choices format) */
    private String extractImageUrl(JsonNode responseNode) {
        try {
            JsonNode output = responseNode.get("output");
            if (output == null) return null;

            // Format 1: output.choices[0].message.content[{image: "url"}]
            JsonNode choices = output.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode message = choices.get(0).get("message");
                if (message != null) {
                    JsonNode content = message.get("content");
                    if (content != null && content.isArray()) {
                        for (JsonNode item : content) {
                            if (item.has("image")) {
                                return item.get("image").asText();
                            }
                        }
                    }
                }
            }

            // Format 2: output.results[0].url
            JsonNode results = output.get("results");
            if (results != null && results.isArray() && results.size() > 0) {
                JsonNode result = results.get(0);
                if (result.has("url")) return result.get("url").asText();
                if (result.has("image") && result.get("image").has("url")) {
                    return result.get("image").get("url").asText();
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract image URL: {}", e.getMessage());
        }
        return null;
    }

    /** Extract task_id for async polling */
    private String extractTaskId(JsonNode responseNode) {
        try {
            JsonNode output = responseNode.get("output");
            if (output != null && output.has("task_id")) {
                return output.get("task_id").asText();
            }
        } catch (Exception e) {
            log.warn("Failed to extract task_id: {}", e.getMessage());
        }
        return null;
    }

    /** Poll for async task result */
    private String pollTask(String taskId, SseEmitter emitter) throws IOException, InterruptedException {
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

            if (output == null) continue;

            String status = output.has("task_status") ? output.get("task_status").asText() : "";

            switch (status) {
                case "SUCCEEDED":
                    // Try the same extraction on poll response
                    String url = extractImageUrl(responseNode);
                    if (url != null) return url;
                    // Fallback: try results[0].url / image.url
                    if (output.has("results") && output.get("results").isArray()
                            && output.get("results").size() > 0) {
                        JsonNode result = output.get("results").get(0);
                        if (result.has("url")) return result.get("url").asText();
                        if (result.has("image") && result.get("image").has("url")) {
                            return result.get("image").get("url").asText();
                        }
                    }
                    log.warn("SUCCEEDED but no URL found: {}", response.body());
                    return null;

                case "FAILED":
                    String errMsg = output.has("message") ? output.get("message").asText() : "未知错误";
                    log.error("Image task failed: {}", errMsg);
                    return null;

                case "CANCELED":
                    log.warn("Image task canceled");
                    return null;

                case "PENDING":
                case "RUNNING":
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

    private void sendError(SseEmitter emitter, String message) {
        sendEvent(emitter, "error",
            "{\"type\":\"error\",\"content\":\"" + escapeJson(message) + "\"}");
        sendEvent(emitter, "done", "[DONE]");
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
