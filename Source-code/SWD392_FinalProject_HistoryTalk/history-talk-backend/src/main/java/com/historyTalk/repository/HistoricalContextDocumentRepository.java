package com.historyTalk.repository;

import com.historyTalk.entity.HistoricalContextDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoricalContextDocumentRepository extends JpaRepository<HistoricalContextDocument, String> {

    List<HistoricalContextDocument> findAllByOrderByUploadDateDesc();

    List<HistoricalContextDocument> findByHistoricalContextContextIdOrderByUploadDateDesc(String contextId);

    List<HistoricalContextDocument> findByStaffStaffIdOrderByUploadDateDesc(String staffId);

    @Query("""
            SELECT hcd FROM HistoricalContextDocument hcd
            WHERE (:search IS NULL OR :search = '' OR
                   LOWER(hcd.title) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(hcd.content) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY hcd.uploadDate DESC
            """)
    List<HistoricalContextDocument> search(@Param("search") String search);
}
