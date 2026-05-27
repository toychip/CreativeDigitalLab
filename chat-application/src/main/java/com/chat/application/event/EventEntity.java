package com.chat.application.event;

import com.chat.application.common.JsonUtil;
import com.chat.domain.event.ChatEvent;
import com.chat.domain.event.LifecycleEvent;
import com.chat.domain.event.MessageEvent;
import com.chat.domain.event.UserEvent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(
        name = "event",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_event_session_client", columnNames = {"session_id", "client_event_id"}),
                @UniqueConstraint(name = "uk_event_session_seq", columnNames = {"session_id", "seq"})
        },
        indexes = {
                @Index(name = "idx_session_created", columnList = "session_id, created_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EventEntity {

    @Id
    @Column(name = "event_id", length = 36, nullable = false, updatable = false)
    private String eventId;

    @Column(name = "session_id", length = 36, nullable = false, updatable = false)
    private String sessionId;

    @Column(name = "client_event_id", length = 36, nullable = false, updatable = false)
    private String clientEventId;

    @Column(name = "seq", nullable = false, updatable = false)
    private long seq;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", length = 20, nullable = false, updatable = false)
    private EventType eventType;

    @Column(name = "payload", columnDefinition = "JSON", nullable = false, updatable = false)
    private String payload;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // domain event -> entity
    public static EventEntity from(ChatEvent event) {
        EventType type = switch (event) {
            case LifecycleEvent ignored -> EventType.LIFECYCLE;
            case UserEvent ignored -> EventType.USER;
            case MessageEvent ignored -> EventType.MESSAGE;
        };
        return new EventEntity(
                event.eventId(),
                event.sessionId(),
                event.clientEventId(),
                event.seq(),
                type,
                JsonUtil.toJson(event),
                event.createdAt()
        );
    }

    // entity -> domain event
    public ChatEvent toDomain() {
        return switch (eventType) {
            case LIFECYCLE -> JsonUtil.fromJson(payload, LifecycleEvent.class);
            case USER -> JsonUtil.fromJson(payload, UserEvent.class);
            case MESSAGE -> JsonUtil.fromJson(payload, MessageEvent.class);
        };
    }
}
