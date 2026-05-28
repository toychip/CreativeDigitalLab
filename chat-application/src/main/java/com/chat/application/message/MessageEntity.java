package com.chat.application.message;

import com.chat.application.common.BaseEntity;
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
public class MessageEntity extends BaseEntity {

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

    public static MessageEntity send(
            String messageId,
            String sessionId,
            String senderId,
            String content,
            long seq
    ) {
        return new MessageEntity(messageId, sessionId, senderId, content, MessageStatus.SENT, seq);
    }

    public void edit(String content) {
        // DELETED 는 terminal — 순서 역전으로 DELETE 뒤 EDIT 가 와도 부활 금지
        if (this.status == MessageStatus.DELETED) {
            return;
        }
        this.content = content;
        this.status = MessageStatus.EDITED;
    }

    public void delete() {
        this.status = MessageStatus.DELETED;
    }
}
