package com.historyTalk.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * HistoricalContextDocument Entity
 * Represents historical documents (PDF, TXT, DOCX) uploaded by staff
 * for RAG (Retrieval-Augmented Generation) knowledge base
 * 
 * Relationships:
 * - ManyToOne: Staff (who uploaded)
 * - ManyToOne: HistoricalContext (which context this document belongs to)
 * 
 * Soft Delete: Use isDeleted flag (not permanently removed)
 */
@Entity
@Table(name = "historical_context_document", indexes = {
        @Index(name = "idx_context_id", columnList = "context_id"),
        @Index(name = "idx_staff_id", columnList = "staff_id"),
        @Index(name = "idx_is_deleted", columnList = "is_deleted")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoricalContextDocument {
    
    @Id
    @Column(name = "doc_id", length = 36)
    private String docId;
    
    @Column(name = "context_id", length = 36, nullable = false)
    private String contextId;
    
    @Column(name = "staff_id", length = 50, nullable = false)
    private String staffId;
    
    @Column(name = "title", length = 255, nullable = false)
    private String title;
    
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "file_format", length = 10, nullable = false)
    private DocumentFileFormat fileFormat;
    
    @Column(name = "file_size", nullable = false)
    private Long fileSize; // in bytes
    
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;
    
    @CreationTimestamp
    @Column(name = "upload_date", nullable = false, updatable = false)
    private LocalDateTime uploadDate;
    
    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
    
    @PrePersist
    public void prePersist() {
        if (this.docId == null) {
            this.docId = UUID.randomUUID().toString();
        }
        if (this.isDeleted == null) {
            this.isDeleted = false;
        }
    }
}
