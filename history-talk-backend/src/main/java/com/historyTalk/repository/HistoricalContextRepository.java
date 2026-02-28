package com.historyTalk.repository;

import com.historyTalk.entity.ContextStatus;
import com.historyTalk.entity.HistoricalContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface HistoricalContextRepository extends JpaRepository<HistoricalContext, String> {
    
    /**
     * Find a historical context by ID excluding deleted ones
     */
    @Query("SELECT hc FROM HistoricalContext hc WHERE hc.contextId = :contextId AND hc.isDeleted = false")
    Optional<HistoricalContext> findByIdNotDeleted(@Param("contextId") String contextId);
    
    /**
     * Find by name (case-insensitive) excluding deleted ones
     */
    @Query("SELECT hc FROM HistoricalContext hc WHERE LOWER(hc.name) = LOWER(:name) AND hc.isDeleted = false")
    Optional<HistoricalContext> findByNameIgnoreCaseNotDeleted(@Param("name") String name);
    
    /**
     * Check if name exists (excluding current record and deleted ones)
     */
    @Query("SELECT CASE WHEN COUNT(hc) > 0 THEN true ELSE false END " +
           "FROM HistoricalContext hc " +
           "WHERE LOWER(hc.name) = LOWER(:name) AND hc.contextId != :contextId AND hc.isDeleted = false")
    Boolean existsByNameIgnoreCaseExcludingId(@Param("name") String name, @Param("contextId") String contextId);
    
    /**
     * Find all non-deleted contexts with pagination and search
     */
    @Query("SELECT hc FROM HistoricalContext hc " +
           "WHERE hc.isDeleted = false AND " +
           "(LOWER(hc.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(hc.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<HistoricalContext> findAllNotDeletedWithSearch(
            @Param("search") String search,
            Pageable pageable
    );
    
    /**
     * Find all non-deleted contexts as simple list (no pagination)
     */
    @Query("SELECT hc FROM HistoricalContext hc " +
           "WHERE hc.isDeleted = false AND " +
           "(LOWER(hc.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(hc.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY hc.createdDate DESC")
    List<HistoricalContext> findAllNotDeletedSimple(@Param("search") String search);
    
    /**
     * Find all non-deleted contexts by status
     */
    @Query("SELECT hc FROM HistoricalContext hc " +
           "WHERE hc.isDeleted = false AND hc.status = :status")
    Page<HistoricalContext> findAllByStatusNotDeleted(
            @Param("status") ContextStatus status,
            Pageable pageable
    );
    
    /**
     * Find by staff ID (contexts created by specific staff)
     */
    @Query("SELECT hc FROM HistoricalContext hc WHERE hc.staffId = :staffId AND hc.isDeleted = false")
    Page<HistoricalContext> findByStaffIdNotDeleted(@Param("staffId") String staffId, Pageable pageable);
}
