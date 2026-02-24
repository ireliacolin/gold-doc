package com.golddoc.repository;

import com.golddoc.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    
    List<Document> findByStatus(String status);
    
    List<Document> findByDocumentType(String documentType);
    
    List<Document> findByCreatedBy(String createdBy);
    
    @Query("SELECT d FROM Document d WHERE LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(d.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Document> searchByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT d FROM Document d WHERE d.tags LIKE %:tag%")
    List<Document> findByTag(@Param("tag") String tag);
    
    Optional<Document> findByTitle(String title);
    
    long countByStatus(String status);
}