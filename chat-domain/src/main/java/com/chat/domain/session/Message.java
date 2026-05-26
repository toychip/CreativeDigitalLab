package com.chat.domain.session;

/**
 * 메시지 한 건
 * MessageEvent 를 누적 적용한 결과로 만들어지며, ChatSession 이 보관한다.
 */
public record Message(
    String messageId,
    String senderId,
    String content,
    MessageStatus status
) {

    public Message withContent(String newContent) {
        return new Message(messageId, senderId, newContent, MessageStatus.EDITED);
    }

    public Message markDeleted() {
        return new Message(messageId, senderId, content, MessageStatus.DELETED);
    }
}
