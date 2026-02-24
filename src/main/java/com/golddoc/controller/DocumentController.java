package com.golddoc.controller;

import com.golddoc.model.Document;
import com.golddoc.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Tag(name = "Document Management", description = "APIs for managing gold industry documents")
public class DocumentController {
    
    private final DocumentService documentService;
    
    @GetMapping
    @Operation(summary = "Get all documents")
    public ResponseEntity<List<Document>> getAllDocuments() {
        return ResponseEntity.ok(documentService.getAllDocuments());
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get document by ID")
    public ResponseEntity<Document> getDocumentById(@PathVariable Long id) {
        return documentService.getDocumentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @Operation(summary = "Create a new document")
    public ResponseEntity<Document> createDocument(@Valid @RequestBody Document document) {
        Document createdDocument = documentService.createDocument(document);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDocument);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing document")
    public ResponseEntity<Document> updateDocument(
            @PathVariable Long id,
            @Valid @RequestBody Document documentDetails) {
        return documentService.updateDocument(id, documentDetails)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a document")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        if (documentService.deleteDocument(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Get documents by status")
    public ResponseEntity<List<Document>> getDocumentsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(documentService.getDocumentsByStatus(status));
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search documents by keyword")
    public ResponseEntity<List<Document>> searchDocuments(@RequestParam String keyword) {
        return ResponseEntity.ok(documentService.searchDocuments(keyword));
    }
    
    @GetMapping("/type/{type}")
    @Operation(summary = "Get documents by type")
    public ResponseEntity<List<Document>> getDocumentsByType(@PathVariable String type) {
        return ResponseEntity.ok(documentService.getDocumentsByType(type));
    }
    
    @GetMapping("/stats")
    @Operation(summary = "Get document statistics")
    public ResponseEntity<Map<String, Object>> getDocumentStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", documentService.getTotalDocumentCount());
        stats.put("draft", documentService.getDocumentCountByStatus("DRAFT"));
        stats.put("review", documentService.getDocumentCountByStatus("REVIEW"));
        stats.put("approved", documentService.getDocumentCountByStatus("APPROVED"));
        stats.put("archived", documentService.getDocumentCountByStatus("ARCHIVED"));
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health check endpoint")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Gold Document Management System");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
}