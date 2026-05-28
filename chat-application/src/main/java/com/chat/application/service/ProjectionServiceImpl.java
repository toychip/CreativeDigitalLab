package com.chat.application.service;

import com.chat.application.message.MessageEntity;
import com.chat.application.message.MessageRepository;
import com.chat.application.session.SessionEntity;
import com.chat.application.session.SessionRepository;
import com.chat.application.sessionuser.MemberRole;
import com.chat.application.sessionuser.SessionUserEntity;
import com.chat.application.sessionuser.SessionUserRepository;
import com.chat.domain.event.LifecycleEvent;
import com.chat.domain.event.MessageEvent;
import com.chat.domain.event.UserEvent;
import com.chat.domain.session.SessionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectionServiceImpl implements ProjectionService {

    private final SessionRepository sessionRepository;
    private final SessionUserRepository sessionUserRepository;
    private final MessageRepository messageRepository;

    @Override
    @Transactional
    public void handleLifecycle(LifecycleEvent event) {
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

    @Override
    @Transactional
    public void handleUser(UserEvent event) {
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

    @Override
    @Transactional
    public void handleMessage(MessageEvent event) {
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
