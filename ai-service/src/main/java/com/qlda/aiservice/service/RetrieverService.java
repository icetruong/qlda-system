package com.qlda.aiservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RetrieverService {
    private final JdbcTemplate jdbcTemplate;

    public List<String> searchByKeyword(String message) {
        String sql = """
            SELECT noi_dung
            FROM ai_document_chunk
            WHERE noi_dung ILIKE ?
            LIMIT 5
        """;

        return jdbcTemplate.queryForList(
                sql,
                String.class,
                "%" + message + "%"
        );
    }
}
