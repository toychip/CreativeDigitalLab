package com.chat.application.session;

import com.chat.application.event.EventEntity;
import com.chat.application.event.EventRepository;
import com.chat.application.sessionuser.SessionUserEntity;
import com.chat.application.sessionuser.SessionUserRepository;
import com.chat.domain.event.ChatEvent;
import com.chat.domain.exception.CdlException;
import com.chat.domain.exception.ExceptionCode;
import com.chat.domain.session.ChatSession;
import com.chat.domain.session.SessionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionQueryServiceImpl implements SessionQueryService {

    private static final int MAX_LIMIT = 100;

    private final SessionRepository sessionRepository;
    private final SessionUserRepository sessionUserRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional(readOnly = true)
    public SessionDetailResponse getSessionDetail(String sessionId) {
        SessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CdlException(ExceptionCode.SESSION_NOT_FOUND));
        List<SessionUserEntity> members = sessionUserRepository.findBySessionIdOrderByJoinedAtAsc(sessionId);
        return SessionDetailResponse.of(session, members);
    }

    @Override
    @Transactional(readOnly = true)
    public SessionPageResponse getSessions(SessionStatus status, LocalDateTime from, LocalDateTime to, String cursor, int limit) {
        int capped = Math.max(1, Math.min(limit, MAX_LIMIT));
        List<SessionEntity> sessions = sessionRepository.findWithCursor(status, from, to, cursor, Limit.of(capped));
        return SessionPageResponse.of(sessions, capped);
    }

    @Override
    @Transactional(readOnly = true)
    public TimelineResponse getTimeline(String sessionId, Instant at) {
        List<EventEntity> entities = (at != null)
                ? eventRepository.findEventsUpTo(sessionId, at)
                : eventRepository.findAllBySessionId(sessionId);
        List<ChatEvent> events = entities.stream().map(EventEntity::toDomain).toList();
        ChatSession session = ChatSession.loadFromEvents(events);
        return TimelineResponse.from(session);
    }
}
