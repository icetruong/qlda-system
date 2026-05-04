// dto/ChatResponse.java
package com.qlda.aiservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatResponse {
    private String reply;
    private boolean success;
    private String errorCode;   // null | "RATE_LIMITED" | "API_ERROR"
    private Long retryAfterMs;  // client biết chờ bao lâu
}
