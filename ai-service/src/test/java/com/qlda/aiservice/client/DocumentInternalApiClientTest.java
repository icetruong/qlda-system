package com.qlda.aiservice.client;

import com.qlda.aiservice.dto.internal.DocumentAttachmentDto;
import com.qlda.aiservice.dto.internal.DocumentContentDto;
import com.qlda.aiservice.dto.internal.DocumentMetadataDto;
import com.qlda.aiservice.exception.AppException;
import com.qlda.aiservice.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpStatus.NOT_FOUND;

class DocumentInternalApiClientTest {

    private static final String BASE_URL = "http://document-service";
    private static final String TOKEN = "internal-token";
    private static final String SERVICE_NAME = "ai-service";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private DocumentInternalApiClient client;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        client = new DocumentInternalApiClient(restTemplate, objectMapper, BASE_URL, TOKEN, SERVICE_NAME);
    }

    @Test
    void shouldCallGetDocumentWithAuthHeadersAndMapData() throws Exception {
        String body = """
            {
              "success": true,
              "message": "ok",
              "data": {
                "id": 1,
                "trichYeu": "Document abstract",
                "loaiVanBanId": 2,
                "donViChuTriId": 3,
                "nguoiTaoId": 4,
                "hanXuLy": "2026-05-10T17:00:00"
              }
            }
            """;

        server.expect(requestTo(BASE_URL + "/internal/documents/1"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("Authorization", "Bearer " + TOKEN))
            .andExpect(header("X-Service-Name", SERVICE_NAME))
            .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        DocumentMetadataDto response = client.getDocument(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.trichYeu()).isEqualTo("Document abstract");
        assertThat(response.loaiVanBanId()).isEqualTo(2L);
        server.verify();
    }

    @Test
    void shouldCallGetDocumentContentWithHeadersAndMapData() throws Exception {
        String body = """
            {
              "success": true,
              "message": "ok",
              "data": {
                "documentId": 2,
                "trichYeu": "A",
                "noiDung": "B",
                "ocrText": "C",
                "language": "vi"
              }
            }
            """;

        server.expect(requestTo(BASE_URL + "/internal/documents/2/content"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("Authorization", "Bearer " + TOKEN))
            .andExpect(header("X-Service-Name", SERVICE_NAME))
            .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        DocumentContentDto response = client.getDocumentContent(2L);

        assertThat(response.documentId()).isEqualTo(2L);
        assertThat(response.noiDung()).isEqualTo("B");
        server.verify();
    }

    @Test
    void shouldCallGetAttachmentsWithHeadersAndMapData() throws Exception {
        String body = """
            {
              "success": true,
              "message": "ok",
              "data": [
                {
                  "id": 10,
                  "tenTep": "van-ban.pdf",
                  "duongDanTep": "/uploads/van-ban.pdf",
                  "loaiTep": "pdf",
                  "kichThuoc": 2000
                }
              ]
            }
            """;

        server.expect(requestTo(BASE_URL + "/internal/documents/1/attachments"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("Authorization", "Bearer " + TOKEN))
            .andExpect(header("X-Service-Name", SERVICE_NAME))
            .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        List<DocumentAttachmentDto> response = client.getDocumentAttachments(1L);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().tenTep()).isEqualTo("van-ban.pdf");
        server.verify();
    }

    @Test
    void shouldCallPatchOcrStatusWithHeaders() {
        server.expect(requestTo(BASE_URL + "/internal/documents/5/ocr-status"))
            .andExpect(method(HttpMethod.PATCH))
            .andExpect(header("Authorization", "Bearer " + TOKEN))
            .andExpect(header("X-Service-Name", SERVICE_NAME))
            .andRespond(withSuccess("""
                {"success": true, "message": "ok", "data": {"documentId": 5, "daOCR": true}}
                """, MediaType.APPLICATION_JSON));

        client.updateOcrStatus(5L, true);

        server.verify();
    }

    @Test
    void shouldThrowDocumentNotFoundWhenRemoteReturns404() {
        server.expect(requestTo(BASE_URL + "/internal/documents/404"))
            .andRespond(withStatus(NOT_FOUND));

        assertThatThrownBy(() -> client.getDocument(404L))
            .isInstanceOf(AppException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.DOCUMENT_NOT_FOUND);
    }

    @Test
    void shouldThrowInternalServerErrorWhenRemoteFails() {
        server.expect(requestTo(BASE_URL + "/internal/documents/1/content"))
            .andRespond(withServerError());

        assertThatThrownBy(() -> client.getDocumentContent(1L))
            .isInstanceOf(AppException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldCallAccessCheckWithHeadersAndMapAllowedIds() {
        String body = """
            {
              "success": true,
              "message": "ok",
              "data": {
                "allowedDocumentIds": [1,3]
              }
            }
            """;

        server.expect(requestTo(BASE_URL + "/internal/documents/access-check"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Authorization", "Bearer " + TOKEN))
            .andExpect(header("X-Service-Name", SERVICE_NAME))
            .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        Set<Long> allowed = client.checkDocumentAccess(2L, List.of(1L, 2L, 3L));

        assertThat(allowed).containsExactlyInAnyOrder(1L, 3L);
        server.verify();
    }

    @Test
    void shouldCallMyUploadedCountWithHeadersAndMapCount() {
        String body = """
            {
              "success": true,
              "message": "ok",
              "data": {
                "userId": 2,
                "count": 12
              }
            }
            """;

        server.expect(requestTo(BASE_URL + "/internal/documents/statistics/my-uploaded-count?userId=2"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("Authorization", "Bearer " + TOKEN))
            .andExpect(header("X-Service-Name", SERVICE_NAME))
            .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        long count = client.getMyUploadedDocumentCount(2L);

        assertThat(count).isEqualTo(12L);
        server.verify();
    }
}
