package com.chat.domain.session;

import com.chat.domain.event.ChatEvent;
import com.chat.domain.event.LifecycleEvent;
import com.chat.domain.event.MessageEvent;
import com.chat.domain.event.UserEvent;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 채팅 세션이 지금 어떤 상태인지를 나타내는 도메인 객체
 * ChatEvent들을 처음부터 차례로 적용해서 만들어진다.
 * DB에 따로 저장되지 않고, 시점 복원이 필요할 때마다 events 테이블에서 이벤트들을 가져와 재구성한다.
 */
public class ChatSession {

    private String sessionId;
    private SessionStatus status;
    private long lastSeq = 0;
    private final Set<String> participants = new LinkedHashSet<>();
    private final Map<String, Message> messages = new LinkedHashMap<>();

    public void apply(ChatEvent event) {
        switch (event) {
            case LifecycleEvent e -> applyLifecycle(e);
            case UserEvent e -> applyUser(e);
            case MessageEvent e -> applyMessage(e);
        }
        this.lastSeq = event.seq();
    }

    // fold
    public static ChatSession loadFromEvents(List<ChatEvent> events) {
        ChatSession session = new ChatSession();
        events.forEach(session::apply);
        return session;
    }

    private void applyLifecycle(LifecycleEvent e) {
        this.sessionId = e.sessionId();
        this.status = e.status();
    }

    private void applyUser(UserEvent e) {
        switch (e.type()) {
            case JOINED -> participants.add(e.userId());
            case LEFT -> participants.remove(e.userId());
        }
    }

    private void applyMessage(MessageEvent e) {
        switch (e.type()) {
            case SENT -> messages.put(
                    e.messageId(),
                    new Message(e.messageId(), e.senderId(), e.content(), MessageStatus.SENT)
            );
            case EDITED -> {
                Message existing = messages.get(e.messageId());
                if (existing != null) {
                    messages.put(e.messageId(), existing.withContent(e.content()));
                }
            }
            case DELETED -> {
                Message existing = messages.get(e.messageId());
                if (existing != null) {
                    messages.put(e.messageId(), existing.markDeleted());
                }
            }
        }
    }

    public String getSessionId() {
        return sessionId;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public long getLastSeq() {
        return lastSeq;
    }

    public Set<String> getParticipants() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(participants));
    }

    public Collection<Message> getMessages() {
        return List.copyOf(messages.values());
    }
}
