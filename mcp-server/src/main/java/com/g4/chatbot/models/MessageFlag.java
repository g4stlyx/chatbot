package com.g4.chatbot.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "message_flags")
public class MessageFlag {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "message_id", nullable = false)
    private Long messageId;
    
    @Column(name = "flagged_by", nullable = false)
    private Long flaggedBy;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "flag_type", nullable = false)
    private FlagType flagType;
    
    @Lob
    @Column(name = "reason")
    private String reason;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FlagStatus status = FlagStatus.PENDING;
    
    @Column(name = "reviewed_by")
    private Long reviewedBy;
    
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // References
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", insertable = false, updatable = false)
    private Message message;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flagged_by", insertable = false, updatable = false)
    private Admin flaggedByAdmin;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by", insertable = false, updatable = false)
    private Admin reviewedByAdmin;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public enum FlagType {
        INAPPROPRIATE, SPAM, HARMFUL, OTHER
    }
    
    public enum FlagStatus {
        PENDING, REVIEWED, RESOLVED, DISMISSED
    }
}