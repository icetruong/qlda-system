package com.qlda.aiservice.service;

import java.util.Map;

public record MetadataOutput(
    Map<String, Object> metadata,
    double confidence,
    String modelUsed
) {
}

