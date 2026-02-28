package com.historyTalk.service;

import com.historyTalk.dto.CreateHistoricalContextDocumentRequest;
import com.historyTalk.dto.HistoricalContextDocumentResponse;
import com.historyTalk.dto.UpdateHistoricalContextDocumentRequest;
import com.historyTalk.entity.DocumentFileFormat;
import com.historyTalk.entity.HistoricalContextDocument;
import com.historyTalk.exception.ResourceNotFoundException;
import com.historyTalk.repository.HistoricalContextDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for HistoricalContextDocument
 * Handles business logic for document management:
 * - Create (upload)
 * - Read (list, search, get by ID)
 * - Update (replace content)
 * - Delete (soft delete - mark as inactive)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HistoricalContextDocumentService {
    
    private final HistoricalContextDocumentRepository documentRepository;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    
    /**
     * Get all active documents
     */
    @Transactional(readOnly = true)
    public List<HistoricalContextDocumentResponse> getAllDocuments() {
        log.info("Fetching all active historical context documents");
        return documentRepository.findAllNotDeleted()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all documents by context ID
     */
    @Transactional(readOnly = true)
    public List<HistoricalContextDocumentResponse> getDocumentsByContextId(String contextId) {
        log.info("Fetching documents for context: {}", contextId);
        return documentRepository.findByContextIdNotDeleted(contextId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all documents by staff ID (audit trail)
     */
    @Transactional(readOnly = true)
    public List<HistoricalContextDocumentResponse> getDocumentsByStaffId(String staffId) {
        log.info("Fetching documents uploaded by staff: {}", staffId);
        return documentRepository.findByStaffIdNotDeleted(staffId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Search documents by title or content
     */
    @Transactional(readOnly = true)
    public List<HistoricalContextDocumentResponse> searchDocuments(String search) {
        log.info("Searching documents with keyword: {}", search);
        return documentRepository.searchNotDeletedByTitleOrContent(search != null ? search : "")
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get single document by ID
     */
    @Transactional(readOnly = true)
    public HistoricalContextDocumentResponse getDocumentById(String docId) {
        log.info("Fetching document: {}", docId);
        HistoricalContextDocument doc = documentRepository.findByIdNotDeleted(docId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + docId));
        return mapToResponse(doc);
    }
    
    /**
     * Create/upload new document
     */
    @Transactional
    public HistoricalContextDocumentResponse createDocument(CreateHistoricalContextDocumentRequest request, String staffId) {
        log.info("Creating document: {} by staff: {}", request.getTitle(), staffId);
        
        // Validate file format
        DocumentFileFormat format;
        try {
            format = DocumentFileFormat.valueOf(request.getFileFormat().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid file format: {}", request.getFileFormat());
            throw new IllegalArgumentException("Unsupported file format. Allowed: PDF, TXT, DOCX");
        }
        
        // Validate file size (estimate from content length)
        long contentSize = request.getContent().getBytes().length;
        if (contentSize > MAX_FILE_SIZE) {
            log.warn("File too large: {} bytes", contentSize);
            throw new IllegalArgumentException("File size exceeds 10MB limit");
        }
        
        HistoricalContextDocument doc = HistoricalContextDocument.builder()
                .contextId(request.getContextId())
                .staffId(staffId)
                .title(request.getTitle())
                .content(request.getContent())
                .fileFormat(format)
                .fileSize(contentSize)
                .isDeleted(false)
                .build();
        
        HistoricalContextDocument saved = documentRepository.save(doc);
        log.info("Document created: {} with ID: {}", request.getTitle(), saved.getDocId());
        return mapToResponse(saved);
    }
    
    /**
     * Update document (replace content and/or title)
     */
    @Transactional
    public HistoricalContextDocumentResponse updateDocument(String docId, UpdateHistoricalContextDocumentRequest request, String staffId) {
        log.info("Updating document: {} by staff: {}", docId, staffId);
        
        HistoricalContextDocument doc = documentRepository.findByIdNotDeleted(docId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + docId));
        
        // Only creator or admin can update
        if (!doc.getStaffId().equals(staffId)) {
            log.warn("Unauthorized update attempt on document {} by staff {}", docId, staffId);
            throw new SecurityException("Only document creator can update");
        }
        
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            doc.setTitle(request.getTitle());
        }
        
        if (request.getContent() != null && !request.getContent().isBlank()) {
            // Validate new content size
            long contentSize = request.getContent().getBytes().length;
            if (contentSize > MAX_FILE_SIZE) {
                log.warn("New content too large: {} bytes", contentSize);
                throw new IllegalArgumentException("File size exceeds 10MB limit");
            }
            doc.setContent(request.getContent());
            doc.setFileSize(contentSize);
        }
        
        HistoricalContextDocument updated = documentRepository.save(doc);
        log.info("Document updated: {}", docId);
        return mapToResponse(updated);
    }
    
    /**
     * Delete document (soft delete - mark as inactive)
     */
    @Transactional
    public void deleteDocument(String docId, String staffId) {
        log.info("Deleting (soft) document: {} by staff: {}", docId, staffId);
        
        HistoricalContextDocument doc = documentRepository.findByIdNotDeleted(docId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + docId));
        
        // Only creator or admin can delete
        if (!doc.getStaffId().equals(staffId)) {
            log.warn("Unauthorized delete attempt on document {} by staff {}", docId, staffId);
            throw new SecurityException("Only document creator can delete");
        }
        
        doc.setIsDeleted(true);
        documentRepository.save(doc);
        log.info("Document marked as deleted: {}", docId);
    }
    
    /**
     * Map entity to response DTO
     */
    private HistoricalContextDocumentResponse mapToResponse(HistoricalContextDocument doc) {
        return HistoricalContextDocumentResponse.builder()
                .docId(doc.getDocId())
                .contextId(doc.getContextId())
                .staffId(doc.getStaffId())
                .title(doc.getTitle())
                .content(doc.getContent())
                .fileFormat(doc.getFileFormat())
                .fileSize(doc.getFileSize())
                .uploadDate(doc.getUploadDate())
                .updatedDate(doc.getUpdatedDate())
                .isDeleted(doc.getIsDeleted())
                .build();
    }
}
