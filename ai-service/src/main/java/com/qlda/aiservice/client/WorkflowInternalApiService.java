package com.qlda.aiservice.client;

public interface WorkflowInternalApiService {
    long getMyDueSoonDocumentCount(Long userId, int days);

    long getMyOverdueDocumentCount(Long userId);
}
