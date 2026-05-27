package com.chat.websocket.listener;

import com.chat.domain.event.ChatEvent;
import com.chat.domain.event.MessageEvent;
import com.chat.websocket.broadcast.GlobalBroadcaster;
import com.chat.websocket.broadcast.LocalBroadcaster;
import com.chat.websocket.dto.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatEventListener {

    private final LocalBroadcaster localBroadcaster;
    private final GlobalBroadcaster globalBroadcaster;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onChatEvent(ChatEvent event) {
        if (event instanceof MessageEvent messageEvent) {
            ChatMessageResponse response = ChatMessageResponse.fromMessage(messageEvent);
            localBroadcaster.broadcast(event.sessionId(), response);
            globalBroadcaster.broadcast(event.sessionId(), response);
        }
    }
}
