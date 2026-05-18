package com.crossborder.model;

public record ChatRequest(
    String message,
    String sessionId
) {}
