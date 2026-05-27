package com.chat.application.listener;

import com.chat.domain.event.ChatEvent;
import com.chat.domain.event.LifecycleEvent;
import com.chat.domain.event.MessageEvent;
import com.chat.domain.event.UserEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ProjectionUpdater {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ChatEvent event) {
        switch (event) {
            case LifecycleEvent e -> {
                // TODO: session_projection 테이블의 status 갱신
            }
            case UserEvent e -> {
                // TODO: participants_projection 테이블에 join/leave 반영
            }
            case MessageEvent e -> {
                // TODO: message_projection 테이블에 SENT/EDITED/DELETED 반영
            }
        }
    }
}
