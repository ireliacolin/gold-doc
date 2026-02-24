package com.golddoc.service;

import com.golddoc.model.Document;
import com.golddoc.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {
    
    @Mock
    private DocumentRepository documentRepository;
    
    @InjectMocks
    private DocumentService documentService;
    
    private Document testDocument;
    
    @BeforeEach
    void setUp() {
        testDocument = new Document();
        testDocument.setId(1L);
        testDocument.setTitle("Test Document");
        testDocument.setDescription("Test Description");
        testDocument.setDocumentType("CONTRACT");
        testDocument.setStatus("DRAFT");
        testDocument.setCreatedBy("testuser");
        testDocument.setFilePath("/documents/test.pdf");
        testDocument.setTags("test,contract");
    }
    
    @Test
    void testGetAllDocuments() {
        // Given
        List<Document> documents = Arrays.asList(testDocument);
        when(documentRepository.findAll()).thenReturn(documents);
        
        // When
        List<Document> result = documentService.getAllDocuments();
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Document", result.get(0).getTitle());
        verify(documentRepository, times(1)).findAll();
    }
    
    @Test
    void testGetDocumentById_Found() {
        // Given
        when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));
        
        // When
        Optional<Document> result = documentService.getDocumentById(1L);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals("Test Document", result.get().getTitle());
        verify(documentRepository, times(1)).findById(1L);
    }
    
    @Test
    void testGetDocumentById_NotFound() {
        // Given
        when(documentRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When
        Optional<Document> result = documentService.getDocumentById(999L);
        
        // Then
        assertFalse(result.isPresent());
        verify(documentRepository, times(1)).findById(999L);
    }
    
    @Test
    void testCreateDocument() {
        // Given
        when(documentRepository.save(any(Document.class))).thenReturn(testDocument);
        
        // When
        Document result = documentService.createDocument(testDocument);
        
        // Then
        assertNotNull(result);
        assertEquals("Test Document", result.getTitle());
        assertEquals("DRAFT", result.getStatus());
        assertNotNull(result.getCreatedAt());
        verify(documentRepository, times(1)).save(any(Document.class));
    }
    
    @Test
    void testUpdateDocument_Success() {
        // Given
        Document updatedDocument = new Document();
        updatedDocument.setTitle("Updated Title");
        updatedDocument.setDescription("Updated Description");
        updatedDocument.setDocumentType("REPORT");
        updatedDocument.setStatus("APPROVED");
        updatedDocument.setFilePath("/documents/updated.pdf");
        updatedDocument.setTags("updated,report");
        
        when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));
        when(documentRepository.save(any(Document.class))).thenReturn(updatedDocument);
        
        // When
        Optional<Document> result = documentService.updateDocument(1L, updatedDocument);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals("Updated Title", result.get().getTitle());
        assertEquals("APPROVED", result.get().getStatus());
        verify(documentRepository, times(1)).findById(1L);
        verify(documentRepository, times(1)).save(any(Document.class));
    }
    
    @Test
    void testUpdateDocument_NotFound() {
        // Given
        when(documentRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When
        Optional<Document> result = documentService.updateDocument(999L, testDocument);
        
        // Then
        assertFalse(result.isPresent());
        verify(documentRepository, times(1)).findById(999L);
        verify(documentRepository, never()).save(any(Document.class));
    }
    
    @Test
    void testDeleteDocument_Success() {
        // Given
        when(documentRepository.existsById(1L)).thenReturn(true);
        
        // When
        boolean result = documentService.deleteDocument(1L);
        
        // Then
        assertTrue(result);
        verify(documentRepository, times(1)).existsById(1L);
        verify(documentRepository, times(1)).deleteById(1L);
    }
    
    @Test
    void testDeleteDocument_NotFound() {
        // Given
        when(documentRepository.existsById(999L)).thenReturn(false);
        
        // When
        boolean result = documentService.deleteDocument(999L);
        
        // Then
        assertFalse(result);
        verify(documentRepository, times(1)).existsById(999L);
        verify(documentRepository, never()).deleteById(anyLong());
    }
    
    @Test
    void testGetTotalDocumentCount() {
        // Given
        when(documentRepository.count()).thenReturn(5L);
        
        // When
        long result = documentService.getTotalDocumentCount();
        
        // Then
        assertEquals(5L, result);
        verify(documentRepository, times(1)).count();
    }
}