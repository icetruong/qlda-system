package com.qlda.aiservice.service;

import java.util.List;

public interface EmbeddingService {
    List<Double> generateEmbedding(String text);
}

