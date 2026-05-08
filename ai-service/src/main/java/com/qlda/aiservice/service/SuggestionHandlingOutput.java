package com.qlda.aiservice.service;

import java.util.List;

public record SuggestionHandlingOutput(
    List<SuggestionItem> suggestions,
    double confidence,
    String modelUsed
) {
}

