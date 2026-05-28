package com.chat.application.session;

import com.chat.application.common.BaseEntity;
import com.chat.domain.session.SessionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "sessions",
        indexes = {
                @Index(name = "idx_session_status", columnList = "status"),
                @Index(name = "idx_session_started_at", columnList = "started_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SessionEntity extends BaseEntity {

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

    public static SessionEntity start(String sessionId) {
        return new SessionEntity(
                sessionId,
                SessionStatus.ACTIVE,
                LocalDateTime.now(),
                null
        );
    }

    public void changeStatus(SessionStatus status) {
        // ENDED 는 terminal — 순서 역전/중복 이벤트로도 되살아나지 않음
        if (this.status == SessionStatus.ENDED) {
            return;
        }
        this.status = status;
        if (status == SessionStatus.ENDED && this.endedAt == null) {
            this.endedAt = LocalDateTime.now();
        }
    }
}
