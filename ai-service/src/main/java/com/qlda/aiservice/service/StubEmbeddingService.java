package com.qlda.aiservice.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StubEmbeddingService implements EmbeddingService {

    @Override
    public List<Double> generateEmbedding(String text) {
        String safe = text == null ? "" : text;
        if (safe.isEmpty()) {
            List<Double> zeros = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                zeros.add(0.0);
            }
            return zeros;
        }

        List<Double> vector = new ArrayList<>();
        int limit = Math.max(8, Math.min(32, safe.length()));
        for (int i = 0; i < limit; i++) {
            char c = safe.charAt(i % safe.length());
            vector.add((double) (c % 32) / 31.0);
        }
        return vector;
    }
}
