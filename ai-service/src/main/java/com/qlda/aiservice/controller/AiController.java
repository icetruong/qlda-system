package com.qlda.aiservice.controller;

import com.qlda.aiservice.dto.common.ApiSuccessResponse;
import com.qlda.aiservice.dto.request.ClassifyRequest;
import com.qlda.aiservice.dto.request.IndexDocumentRequest;
import com.qlda.aiservice.dto.request.MetadataExtractRequest;
import com.qlda.aiservice.dto.request.SemanticSearchRequest;
import com.qlda.aiservice.dto.request.SuggestionHandlingRequest;
import com.qlda.aiservice.dto.request.SuggestionReplyRequest;
import com.qlda.aiservice.dto.request.SummarizeRequest;
import com.qlda.aiservice.service.AiApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiApplicationService aiApplicationService;

    @PostMapping("/summarize")
    public ResponseEntity<ApiSuccessResponse<Map<String, Object>>> summarize(@Valid @RequestBody SummarizeRequest request) {
        return ResponseEntity.ok(ApiSuccessResponse.of("Summarize document successfully", aiApplicationService.summarize(request)));
    }

    @PostMapping("/summarize/file")
    public ResponseEntity<ApiSuccessResponse<Map<String, Object>>> summarizeFile(
        @RequestParam Long documentId,
        @RequestParam Long userId,
        @RequestParam String summaryType,
        @RequestParam(required = false, defaultValue = "vi") String language,
        @RequestParam MultipartFile file
    ) {
        return ResponseEntity.ok(ApiSuccessResponse.of(
            "Summarize file successfully",
            aiApplicationService.summarizeFile(documentId, userId, summaryType, language, file)
        ));
    }

    @PostMapping("/classify")
    public ResponseEntity<ApiSuccessResponse<Map<String, Object>>> classify(@Valid @RequestBody ClassifyRequest request) {
        return ResponseEntity.ok(ApiSuccessResponse.of("Classify document successfully", aiApplicationService.classify(request)));
    }

    @PostMapping("/classify/file")
    public ResponseEntity<ApiSuccessResponse<Map<String, Object>>> classifyFile(
        @RequestParam Long documentId,
        @RequestParam Long userId,
        @RequestParam(required = false, defaultValue = "vi") String language,
        @RequestParam MultipartFile file
    ) {
        return ResponseEntity.ok(ApiSuccessResponse.of(
            "Classify file successfully",
            aiApplicationService.classifyFile(documentId, userId, language, file)
        ));
    }

    @PostMapping("/metadata/extract")
    public ResponseEntity<ApiSuccessResponse<Map<String, Object>>> extractMetadata(@Valid @RequestBody MetadataExtractRequest request) {
        return ResponseEntity.ok(ApiSuccessResponse.of(
            "Extract metadata successfully",
            aiApplicationService.extractMetadata(request)
        ));
    }

    @PostMapping("/metadata/extract/file")
    public ResponseEntity<ApiSuccessResponse<Map<String, Object>>> extractMetadataFile(
        @RequestParam Long documentId,
        @RequestParam Long userId,
        @RequestParam(required = false, defaultValue = "vi") String language,
        @RequestParam MultipartFile file
    ) {
        return ResponseEntity.ok(ApiSuccessResponse.of(
            "Extract metadata from file successfully",
            aiApplicationService.extractMetadataFile(documentId, userId, language, file)
        ));
    }

    @PostMapping("/search/semantic")
    public ResponseEntity<ApiSuccessResponse<Map<String, Object>>> semanticSearch(@Valid @RequestBody SemanticSearchRequest request) {
        return ResponseEntity.ok(ApiSuccessResponse.of("Semantic search successfully", aiApplicationService.semanticSearch(request)));
    }

    @PostMapping("/search/index-document")
    public ResponseEntity<ApiSuccessResponse<Map<String, Object>>> indexDocument(@Valid @RequestBody IndexDocumentRequest request) {
        return ResponseEntity.ok(ApiSuccessResponse.of("Index document successfully", aiApplicationService.indexDocument(request)));
    }

    @DeleteMapping("/search/index-document/{documentId}")
    public ResponseEntity<ApiSuccessResponse<Map<String, Object>>> deleteDocumentIndex(@PathVariable Long documentId) {
        return ResponseEntity.ok(ApiSuccessResponse.of(
            "Delete document index successfully",
            aiApplicationService.deleteDocumentIndex(documentId)
        ));
    }

    @PostMapping("/suggestions/handling")
    public ResponseEntity<ApiSuccessResponse<Map<String, Object>>> suggestHandling(@Valid @RequestBody SuggestionHandlingRequest request) {
        return ResponseEntity.ok(ApiSuccessResponse.of(
            "Generate handling suggestion successfully",
            aiApplicationService.suggestHandling(request)
        ));
    }

    @PostMapping("/suggestions/reply")
    public ResponseEntity<ApiSuccessResponse<Map<String, Object>>> suggestReply(@Valid @RequestBody SuggestionReplyRequest request) {
        return ResponseEntity.ok(ApiSuccessResponse.of(
            "Generate reply suggestion successfully",
            aiApplicationService.suggestReply(request)
        ));
    }

    @GetMapping("/results/documents/{documentId}")
    public ResponseEntity<ApiSuccessResponse<Map<String, Object>>> getResultsByDocument(
        @PathVariable Long documentId,
        @RequestParam(required = false) String loaiXuLyAI,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiSuccessResponse.of(
            "Get AI results successfully",
            aiApplicationService.getResultsByDocument(documentId, loaiXuLyAI, page, size)
        ));
    }

    @GetMapping("/results/{id}")
    public ResponseEntity<ApiSuccessResponse<Map<String, Object>>> getResultDetail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiSuccessResponse.of("Get AI result detail successfully", aiApplicationService.getResultDetail(id)));
    }

    @DeleteMapping("/results/{id}")
    public ResponseEntity<ApiSuccessResponse<Map<String, Object>>> deleteResult(@PathVariable Long id) {
        return ResponseEntity.ok(ApiSuccessResponse.of("Delete AI result successfully", aiApplicationService.deleteResult(id)));
    }
}
