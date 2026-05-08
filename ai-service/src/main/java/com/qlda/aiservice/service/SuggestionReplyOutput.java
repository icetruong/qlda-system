package com.qlda.aiservice.service;

public record SuggestionReplyOutput(
    String suggestedReply,
    String replyStyle,
    double confidence,
    String modelUsed
) {
}

