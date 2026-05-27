package com.chat.application.listener;

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

    @Async
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
            log.error("Projection update failed. eventId={}, sessionId={}", event.eventId(), event.sessionId(), ex);
        }
    }

    private void handleLifecycle(LifecycleEvent event) {
        sessionRepository.findById(event.sessionId()).ifPresentOrElse(
            entity -> {
                if (entity.getStatus() == SessionStatus.ENDED) {
                    log.warn("Lifecycle event on ENDED session, ignoring. sessionId={}, incomingStatus={}",
                        event.sessionId(), event.status());
                    return;
                }
                entity.changeStatus(event.status());
            },
            () -> {
                if (event.status() == SessionStatus.ACTIVE) {
                    sessionRepository.save(SessionEntity.start(event.sessionId()));
                } else {
                    log.warn("No session found and status is not ACTIVE, ignoring. sessionId={}, status={}",
                        event.sessionId(), event.status());
                }
            }
        );
    }

    private void handleUser(UserEvent event) {
        switch (event.type()) {
            case JOINED -> {
                if (sessionUserRepository.existsBySessionIdAndUserIdAndIsActiveTrue(
                        event.sessionId(), event.userId())) {
                    return; // 이미 활성 참여 중 — 멱등
                }
                sessionUserRepository.findBySessionIdAndUserId(event.sessionId(), event.userId())
                    .ifPresentOrElse(
                        entity -> entity.rejoin(),  // 퇴장 이력 있는 row 재활성화
                        () -> sessionUserRepository.save(
                            SessionUserEntity.join(event.sessionId(), event.userId(), MemberRole.MEMBER)
                        )
                    );
            }
            case LEFT -> sessionUserRepository.leaveSession(event.sessionId(), event.userId());
        }
    }

    private void handleMessage(MessageEvent event) {
        switch (event.type()) {
            case SENT -> {
                if (messageRepository.existsById(event.messageId())) return; // 멱등
                messageRepository.save(MessageEntity.send(
                    event.messageId(), event.sessionId(), event.senderId(),
                    event.content(), event.seq(), event.createdAt()
                ));
            }
            case EDITED -> messageRepository.findById(event.messageId()).ifPresentOrElse(
                entity -> entity.edit(event.content()),
                () -> log.warn("EDITED event for unknown messageId={}", event.messageId())
            );
            case DELETED -> messageRepository.findById(event.messageId()).ifPresentOrElse(
                MessageEntity::delete,
                () -> log.warn("DELETED event for unknown messageId={}", event.messageId())
            );
        }
    }
}
