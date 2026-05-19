package com.crossborder.controller;

import com.crossborder.service.ImageGenService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ImageController {

    private final ImageGenService imageGenService;

    public ImageController(ImageGenService imageGenService) {
        this.imageGenService = imageGenService;
    }

    @PostMapping(value = "/generate-image", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generateImage(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        SseEmitter emitter = new SseEmitter(180_000L); // 3 min timeout for image gen
        imageGenService.generateImage(emitter, prompt);
        return emitter;
    }
}
