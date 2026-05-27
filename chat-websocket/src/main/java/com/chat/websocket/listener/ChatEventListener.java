package com.chat.websocket.listener;

import com.chat.domain.event.ChatEvent;
import com.chat.domain.event.MessageEvent;
import com.chat.websocket.broadcast.RedisMessageBroker;
import com.chat.websocket.dto.ChatMessageResponse;
import com.chat.websocket.registry.WsConnectionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatEventListener {

    private final WsConnectionRegistry registry;
    private final RedisMessageBroker broker;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onChatEvent(ChatEvent event) {
        if (event instanceof MessageEvent messageEvent) {
            ChatMessageResponse response = ChatMessageResponse.fromMessage(messageEvent);
            registry.sendMessageToLocalSession(event.sessionId(), response);
            broker.broadcast(event.sessionId(), response);
        }
    }
}
