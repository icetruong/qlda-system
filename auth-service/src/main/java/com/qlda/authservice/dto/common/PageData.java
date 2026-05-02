package com.qlda.authservice.dto.common;

import java.util.List;

public record PageData<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
