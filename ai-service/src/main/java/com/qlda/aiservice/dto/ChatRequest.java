// dto/ChatRequest.java
package com.qlda.aiservice.dto;

import lombok.Data;

@Data
public class ChatRequest {
    private String message;
    private String sessionId; // để rate limit theo session
}
