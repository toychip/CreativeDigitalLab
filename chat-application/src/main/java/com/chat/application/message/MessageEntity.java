package com.chat.application.message;

import com.chat.domain.session.MessageStatus;
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

import java.time.Instant;

@Entity
@Table(
        name = "messages",
        indexes = {
                @Index(name = "idx_message_session_seq", columnList = "session_id, seq"),
                @Index(name = "idx_message_sender", columnList = "sender_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageEntity {

    @Id
    @Column(name = "message_id", length = 36, nullable = false, updatable = false)
    private String messageId;

    @Column(name = "session_id", length = 36, nullable = false)
    private String sessionId;

    @Column(name = "sender_id", length = 36, nullable = false)
    private String senderId;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageStatus status;

    @Column(nullable = false)
    private long seq;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public static MessageEntity send(
            String messageId,
            String sessionId,
            String senderId,
            String content,
            long seq,
            Instant createdAt
    ) {
        return new MessageEntity(messageId, sessionId, senderId, content, MessageStatus.SENT, seq, createdAt);
    }

    public void edit(String content) {
        this.content = content;
        this.status = MessageStatus.EDITED;
    }

    public void delete() {
        this.status = MessageStatus.DELETED;
    }
}
