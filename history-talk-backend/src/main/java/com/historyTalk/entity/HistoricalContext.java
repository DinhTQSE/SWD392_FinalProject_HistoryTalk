package com.historyTalk.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "historical_context")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoricalContext {
    
    @Id
    @Column(name = "context_id", length = 36)
    private String contextId;
    
    @Column(name = "name", nullable = false, length = 100, unique = true)
    private String name;
    
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ContextStatus status;
    
    @Column(name = "staff_id", nullable = false, length = 36)
    private String staffId;
    
    @Column(name = "staff_name", length = 100)
    private String staffName;
    
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;
    
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
    
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;
    
    @PrePersist
    protected void onCreate() {
        this.contextId = UUID.randomUUID().toString();
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
        this.isDeleted = false;
        if (this.status == null) {
            this.status = ContextStatus.DRAFT;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
}
