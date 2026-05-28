package com.chat.application.listener;

import com.chat.application.config.AsyncConfig;

import com.chat.application.service.ProjectionService;
import com.chat.domain.event.ChatEvent;
import com.chat.domain.event.LifecycleEvent;
import com.chat.domain.event.MessageEvent;
import com.chat.domain.event.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectionUpdater {

    private final ProjectionService projectionService;

    @Async(AsyncConfig.PROJECTION_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ChatEvent event) {
        try {
            switch (event) {
                case LifecycleEvent e -> projectionService.handleLifecycle(e);
                case UserEvent e -> projectionService.handleUser(e);
                case MessageEvent e -> projectionService.handleMessage(e);
            }
        } catch (Exception ex) {
            // TODO Production 환경에서 트래픽 파악하여 재시도 규칙 설정 (projection 실패 시 재시도/DLQ)
            log.error("Projection update failed. eventId={}, sessionId={}", event.eventId(), event.sessionId(), ex);
        }
    }


}
