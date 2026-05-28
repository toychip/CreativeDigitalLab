package com.chat.application.listener;

import com.chat.application.config.AsyncConfig;
import com.chat.application.message.MessageEntity;
import com.chat.application.message.MessageRepository;
import com.chat.application.session.SessionEntity;
import com.chat.application.session.SessionRepository;
import com.chat.application.sessionuser.MemberRole;
import com.chat.application.sessionuser.SessionUserEntity;
import com.chat.application.sessionuser.SessionUserRepository;
import com.chat.domain.event.ChatEvent;
import com.chat.domain.event.LifecycleEvent;
import com.chat.domain.event.MessageEvent;
import com.chat.domain.event.UserEvent;
import com.chat.domain.session.SessionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectionUpdater {

    private final SessionRepository sessionRepository;
    private final SessionUserRepository sessionUserRepository;
    private final MessageRepository messageRepository;

    @Async(AsyncConfig.PROJECTION_EXECUTOR)
    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ChatEvent event) {
        try {
            switch (event) {
                case LifecycleEvent e -> handleLifecycle(e);
                case UserEvent e -> handleUser(e);
                case MessageEvent e -> handleMessage(e);
            }
        } catch (Exception ex) {
            // TODO Production 환경에서 트래픽 파악하여 재시도 규칙 설정 (projection 실패 시 재시도/DLQ)
            log.error("Projection update failed. eventId={}, sessionId={}", event.eventId(), event.sessionId(), ex);
        }
    }

    private void handleLifecycle(LifecycleEvent event) {
        SessionEntity session = sessionRepository.findById(event.sessionId()).orElse(null);

        if (session == null) {
            createSessionIfActive(event);
            return;
        }

        if (session.getStatus() == SessionStatus.ENDED) {
            log.warn("Lifecycle event on ENDED session, ignoring. sessionId={}, incomingStatus={}",
                    event.sessionId(), event.status());
            return;
        }

        session.changeStatus(event.status());
    }

    private void createSessionIfActive(LifecycleEvent event) {
        if (event.status() != SessionStatus.ACTIVE) {
            log.warn("No session found and status is not ACTIVE, ignoring. sessionId={}, status={}",
                    event.sessionId(), event.status());
            return;
        }
        sessionRepository.save(SessionEntity.start(event.sessionId()));
    }

    private void handleUser(UserEvent event) {
        switch (event.type()) {
            case JOINED -> handleUserJoined(event);
            case LEFT -> sessionUserRepository.leaveSession(event.sessionId(), event.userId());
        }
    }

    private void handleUserJoined(UserEvent event) {
        boolean alreadyActive = sessionUserRepository.existsBySessionIdAndUserIdAndIsActiveTrue(
                event.sessionId(), event.userId());
        if (alreadyActive) {
            return;
        }

        SessionUserEntity previous = sessionUserRepository
                .findBySessionIdAndUserId(event.sessionId(), event.userId())
                .orElse(null);

        if (previous != null) {
            previous.rejoin();
            return;
        }

        sessionUserRepository.save(
                SessionUserEntity.join(event.sessionId(), event.userId(), MemberRole.MEMBER));
    }

    private void handleMessage(MessageEvent event) {
        switch (event.type()) {
            case SENT -> handleMessageSent(event);
            case EDITED -> handleMessageEdited(event);
            case DELETED -> handleMessageDeleted(event);
        }
    }

    private void handleMessageSent(MessageEvent event) {
        if (messageRepository.existsById(event.messageId())) {
            return;
        }
        messageRepository.save(MessageEntity.send(
                event.messageId(), event.sessionId(), event.senderId(),
                event.content(), event.seq()));
    }

    private void handleMessageEdited(MessageEvent event) {
        MessageEntity message = messageRepository.findById(event.messageId()).orElse(null);
        if (message == null) {
            // TODO Production 환경에서 트래픽 파악하여 재시도 규칙 설정 (순서 역전으로 SENT 미도착 시 재시도)
            log.warn("EDITED event for unknown messageId={}", event.messageId());
            return;
        }
        message.edit(event.content());
    }

    private void handleMessageDeleted(MessageEvent event) {
        MessageEntity message = messageRepository.findById(event.messageId()).orElse(null);
        if (message == null) {
            // TODO Production 환경에서 트래픽 파악하여 재시도 규칙 설정 (순서 역전으로 SENT 미도착 시 재시도)
            log.warn("DELETED event for unknown messageId={}", event.messageId());
            return;
        }
        message.delete();
    }
}
