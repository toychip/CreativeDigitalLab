package com.chat.application.listener;

import com.chat.domain.event.ChatEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// Redis Pub/Sub 전파
@Component
public class RedisBroadcastPublisher {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ChatEvent event) {
        // TODO: convertAndSend(channel, envelope(event, serverId))
    }
}
