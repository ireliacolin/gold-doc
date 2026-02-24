package com.golddoc.service;

import com.golddoc.model.Document;
import com.golddoc.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {
    
    private final DocumentRepository documentRepository;
    
    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }
    
    public Optional<Document> getDocumentById(Long id) {
        return documentRepository.findById(id);
    }
    
    @Transactional
    public Document createDocument(Document document) {
        document.setCreatedAt(LocalDateTime.now());
        document.setStatus("DRAFT");
        return documentRepository.save(document);
    }
    
    @Transactional
    public Optional<Document> updateDocument(Long id, Document documentDetails) {
        return documentRepository.findById(id)
                .map(existingDocument -> {
                    existingDocument.setTitle(documentDetails.getTitle());
                    existingDocument.setDescription(documentDetails.getDescription());
                    existingDocument.setDocumentType(documentDetails.getDocumentType());
                    existingDocument.setStatus(documentDetails.getStatus());
                    existingDocument.setFilePath(documentDetails.getFilePath());
                    existingDocument.setTags(documentDetails.getTags());
                    existingDocument.onUpdate();
                    return documentRepository.save(existingDocument);
                });
    }
    
    @Transactional
    public boolean deleteDocument(Long id) {
        if (documentRepository.existsById(id)) {
            documentRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    public List<Document> getDocumentsByStatus(String status) {
        return documentRepository.findByStatus(status);
    }
    
    public List<Document> searchDocuments(String keyword) {
        return documentRepository.searchByKeyword(keyword);
    }
    
    public List<Document> getDocumentsByType(String type) {
        return documentRepository.findByDocumentType(type);
    }
    
    public long getDocumentCountByStatus(String status) {
        return documentRepository.countByStatus(status);
    }
    
    public long getTotalDocumentCount() {
        return documentRepository.count();
    }
}