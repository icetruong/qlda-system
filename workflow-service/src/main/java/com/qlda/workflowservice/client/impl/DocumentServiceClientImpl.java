package com.qlda.workflowservice.client.impl;

import com.qlda.workflowservice.client.DocumentServiceClient;
import com.qlda.workflowservice.client.dto.DocumentAssigneeUpdateRequest;
import com.qlda.workflowservice.client.dto.DocumentDetailDto;
import com.qlda.workflowservice.client.dto.DocumentStatusUpdateRequest;
import com.qlda.workflowservice.client.dto.DocumentWorkflowStatusUpdateRequest;
import com.qlda.workflowservice.client.internal.DocumentServiceHttpClient;
import com.qlda.workflowservice.exception.ApiException;
import com.qlda.workflowservice.exception.ErrorCode;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class DocumentServiceClientImpl implements DocumentServiceClient {
    private final DocumentServiceHttpClient documentServiceHttpClient;

    @Override
    public DocumentDetailDto getDocumentById(Long id) {
        return execute(() -> documentServiceHttpClient.getDocumentById(id));
    }

    @Override
    public void updateDocumentStatus(Long id, DocumentStatusUpdateRequest request) {
        executeVoid(() -> documentServiceHttpClient.updateDocumentStatus(id, request));
    }

    @Override
    public void updateDocumentAssignee(Long id, DocumentAssigneeUpdateRequest request) {
        executeVoid(() -> documentServiceHttpClient.updateDocumentAssignee(id, request));
    }

    @Override
    public void updateDocumentWorkflowStatus(Long id, DocumentWorkflowStatusUpdateRequest request) {
        executeVoid(() -> documentServiceHttpClient.updateDocumentWorkflowStatus(id, request));
    }

    private <T> T execute(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (FeignException exception) {
            throw mapFeignException(exception);
        }
    }

    private void executeVoid(Runnable runnable) {
        try {
            runnable.run();
        } catch (FeignException exception) {
            throw mapFeignException(exception);
        }
    }

    private ApiException mapFeignException(FeignException exception) {
        if (exception.status() == 404) {
            return new ApiException(ErrorCode.DOCUMENT_NOT_FOUND, HttpStatus.NOT_FOUND, "Document not found");
        }
        return new ApiException(ErrorCode.INTERNAL_SERVER_ERROR, HttpStatus.BAD_GATEWAY, "Document service request failed");
    }
}
