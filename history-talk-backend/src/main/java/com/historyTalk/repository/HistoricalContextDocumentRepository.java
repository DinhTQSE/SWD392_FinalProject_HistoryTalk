package com.historyTalk.repository;

import com.historyTalk.entity.HistoricalContextDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for HistoricalContextDocument
 * Handles database queries with soft delete filtering
 */
@Repository
public interface HistoricalContextDocumentRepository extends JpaRepository<HistoricalContextDocument, String> {
    
    /**
     * Find all active documents (not deleted)
     */
    @Query("SELECT hcd FROM HistoricalContextDocument hcd WHERE hcd.isDeleted = false ORDER BY hcd.uploadDate DESC")
    List<HistoricalContextDocument> findAllNotDeleted();
    
    /**
     * Find all active documents by context ID
     */
    @Query("SELECT hcd FROM HistoricalContextDocument hcd WHERE hcd.contextId = :contextId AND hcd.isDeleted = false ORDER BY hcd.uploadDate DESC")
    List<HistoricalContextDocument> findByContextIdNotDeleted(@Param("contextId") String contextId);
    
    /**
     * Find all active documents by staff ID
     */
    @Query("SELECT hcd FROM HistoricalContextDocument hcd WHERE hcd.staffId = :staffId AND hcd.isDeleted = false ORDER BY hcd.uploadDate DESC")
    List<HistoricalContextDocument> findByStaffIdNotDeleted(@Param("staffId") String staffId);
    
    /**
     * Search active documents by title or content
     */
    @Query("SELECT hcd FROM HistoricalContextDocument hcd WHERE hcd.isDeleted = false AND (LOWER(hcd.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(hcd.content) LIKE LOWER(CONCAT('%', :search, '%'))) ORDER BY hcd.uploadDate DESC")
    List<HistoricalContextDocument> searchNotDeletedByTitleOrContent(@Param("search") String search);
    
    /**
     * Find active document by ID
     */
    @Query("SELECT hcd FROM HistoricalContextDocument hcd WHERE hcd.docId = :docId AND hcd.isDeleted = false")
    Optional<HistoricalContextDocument> findByIdNotDeleted(@Param("docId") String docId);
}
