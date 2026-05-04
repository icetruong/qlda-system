// dto/ChatRequest.java
package com.qlda.ai_service.dto;

import lombok.Data;

@Data
public class ChatRequest {
    private String message;
    private String sessionId; // để rate limit theo session
}