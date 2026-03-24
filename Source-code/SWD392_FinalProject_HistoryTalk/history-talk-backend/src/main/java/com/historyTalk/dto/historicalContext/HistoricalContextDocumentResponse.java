package com.historyTalk.dto.historicalContext;

import com.historyTalk.entity.enums.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoricalContextDocumentResponse {

    private String docId;
    private String contextId;
    private String uid;
    private String userName;
    private String title;
    private String content;
    private DocumentType type;
    private LocalDateTime uploadDate;
    private LocalDateTime updatedDate;
    private LocalDateTime deletedAt;
}
