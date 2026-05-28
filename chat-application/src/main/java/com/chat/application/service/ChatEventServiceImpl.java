package com.chat.application.service;

import com.chat.application.event.EventEntity;
import com.chat.application.event.EventRepository;
import com.chat.application.sequence.SequenceGenerator;
import com.chat.application.service.command.LifecycleCommand;
import com.chat.application.service.command.MessageCommand;
import com.chat.application.service.command.UserCommand;
import com.chat.application.session.SessionRepository;
import com.chat.domain.event.LifecycleEvent;
import com.chat.domain.event.MessageEvent;
import com.chat.domain.event.UserEvent;
import com.chat.domain.exception.CdlException;
import com.chat.domain.exception.ExceptionCode;
import com.chat.domain.session.SessionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatEventServiceImpl implements ChatEventService {

    private final EventRepository eventRepository;
    private final SequenceGenerator sequenceGenerator;
    private final ApplicationEventPublisher eventPublisher;
    private final SessionRepository sessionRepository;

    @Override
    @Transactional
    public void createSession(String sessionId, String clientEventId, String creatorUserId) {
        long lifecycleSeq = sequenceGenerator.nextSeq(sessionId);
        LifecycleEvent lifecycle = LifecycleEvent.create(
            sessionId, clientEventId, lifecycleSeq, SessionStatus.ACTIVE
        );
        eventRepository.saveAndFlush(EventEntity.from(lifecycle));

        long userSeq = sequenceGenerator.nextSeq(sessionId);
        UserEvent user = UserEvent.create(
            sessionId, clientEventId, userSeq, creatorUserId, UserEvent.Type.JOINED
        );
        eventRepository.saveAndFlush(EventEntity.from(user));

        eventPublisher.publishEvent(lifecycle);
        eventPublisher.publishEvent(user);
    }

    @Override
    @Transactional
    public Optional<LifecycleEvent> appendLifecycle(LifecycleCommand command) {
        ensureNotEnded(command.sessionId());
        long seq = sequenceGenerator.nextSeq(command.sessionId());
        LifecycleEvent event = LifecycleEvent.create(
            command.sessionId(), command.clientEventId(), seq, command.status()
        );
        try {
            eventRepository.saveAndFlush(EventEntity.from(event));
        } catch (DataIntegrityViolationException e) {
            return Optional.empty();
        }
        eventPublisher.publishEvent(event);
        return Optional.of(event);
    }

    @Override
    @Transactional
    public Optional<UserEvent> appendUser(UserCommand command) {
        if (command.type() == UserEvent.Type.JOINED) {
            ensureNotEnded(command.sessionId());
        }
        long seq = sequenceGenerator.nextSeq(command.sessionId());
        UserEvent event = UserEvent.create(
            command.sessionId(), command.clientEventId(), seq,
            command.userId(), command.type()
        );
        try {
            eventRepository.saveAndFlush(EventEntity.from(event));
        } catch (DataIntegrityViolationException e) {
            return Optional.empty();
        }
        eventPublisher.publishEvent(event);
        return Optional.of(event);
    }

    // ENDED 세션에 추가 lifecycle/join 차단 (read model 기준)
    private void ensureNotEnded(String sessionId) {
        sessionRepository.findById(sessionId).ifPresent(session -> {
            if (session.getStatus() == SessionStatus.ENDED) {
                throw new CdlException(ExceptionCode.SESSION_ALREADY_ENDED);
            }
        });
    }

    @Override
    @Transactional
    public Optional<MessageEvent> appendMessage(MessageCommand command) {
        long seq = sequenceGenerator.nextSeq(command.sessionId());
        MessageEvent event = MessageEvent.create(
            command.sessionId(), command.clientEventId(), seq,
            command.senderId(), command.messageId(), command.content(), command.type()
        );
        try {
            eventRepository.saveAndFlush(EventEntity.from(event));
        } catch (DataIntegrityViolationException e) {
            return Optional.empty();
        }
        eventPublisher.publishEvent(event);
        return Optional.of(event);
    }
}
