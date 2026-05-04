package com.qlda.workflowservice.client;

import com.qlda.workflowservice.client.dto.DocumentAssigneeUpdateRequest;
import com.qlda.workflowservice.client.dto.DocumentDetailDto;
import com.qlda.workflowservice.client.dto.DocumentStatusUpdateRequest;
import com.qlda.workflowservice.client.dto.DocumentWorkflowStatusUpdateRequest;
import com.qlda.workflowservice.client.impl.DocumentServiceClientImpl;
import com.qlda.workflowservice.client.internal.DocumentServiceHttpClient;
import com.qlda.workflowservice.exception.ApiException;
import com.qlda.workflowservice.exception.ErrorCode;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceClientTest {

    @Mock
    private DocumentServiceHttpClient documentServiceHttpClient;

    @InjectMocks
    private DocumentServiceClientImpl documentServiceClient;

    @Test
    void getDocumentById_success() {
        when(documentServiceHttpClient.getDocumentById(1L))
                .thenReturn(new DocumentDetailDto(1L, "123", "test", 1, "Cong van", "INCOMING", 1, 2L, LocalDateTime.now(), 1, false, false));

        DocumentDetailDto response = documentServiceClient.getDocumentById(1L);

        assertEquals(1L, response.id());
    }

    @Test
    void getDocumentById_notFound_shouldThrowApiException() {
        FeignException notFound = FeignException.errorStatus(
                "getDocumentById",
                feign.Response.builder().status(404).reason("Not Found").request(
                        feign.Request.create(feign.Request.HttpMethod.GET, "/internal/documents/1",
                                java.util.Map.of(), null, StandardCharsets.UTF_8, null)).build()
        );
        when(documentServiceHttpClient.getDocumentById(1L)).thenThrow(notFound);

        ApiException exception = assertThrows(ApiException.class, () -> documentServiceClient.getDocumentById(1L));

        assertEquals(ErrorCode.DOCUMENT_NOT_FOUND, exception.getErrorCode());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }

    @Test
    void updateEndpoints_success() {
        doNothing().when(documentServiceHttpClient).updateDocumentStatus(any(), any());
        doNothing().when(documentServiceHttpClient).updateDocumentAssignee(any(), any());
        doNothing().when(documentServiceHttpClient).updateDocumentWorkflowStatus(any(), any());

        documentServiceClient.updateDocumentStatus(1L, new DocumentStatusUpdateRequest(3));
        documentServiceClient.updateDocumentAssignee(1L, new DocumentAssigneeUpdateRequest(2L, 1));
        documentServiceClient.updateDocumentWorkflowStatus(1L, new DocumentWorkflowStatusUpdateRequest("PROCESSING", 10L));
    }
}
