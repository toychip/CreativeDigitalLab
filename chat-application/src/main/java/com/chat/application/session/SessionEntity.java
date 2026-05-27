package com.chat.application.session;

import com.chat.domain.session.SessionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "sessions",
        indexes = {
                @Index(name = "idx_session_status", columnList = "status"),
                @Index(name = "idx_session_started_at", columnList = "started_at")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SessionEntity {

    @Id
    @Column(name = "session_id", length = 36, nullable = false, updatable = false)
    private String sessionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SessionStatus status;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime endedAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static SessionEntity start(String sessionId) {
        return new SessionEntity(
                sessionId,
                SessionStatus.ACTIVE,
                LocalDateTime.now(),
                null,
                null
        );
    }

    public void changeStatus(SessionStatus status) {
        this.status = status;
        if (status == SessionStatus.ENDED && this.endedAt == null) {
            this.endedAt = LocalDateTime.now();
        }
    }
}
