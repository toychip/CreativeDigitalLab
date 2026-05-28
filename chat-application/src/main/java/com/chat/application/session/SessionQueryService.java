package com.chat.application.session;

import com.chat.domain.session.SessionStatus;

import java.time.Instant;
import java.time.LocalDateTime;

public interface SessionQueryService {

    SessionDetailResponse getSessionDetail(String sessionId);

    SessionPageResponse getSessions(SessionStatus status, LocalDateTime from, LocalDateTime to, String cursor, int limit);

    /** 특정 시점(at) 상태 복원 — 이벤트 fold. at 이 null 이면 현재 기준 전체 */
    TimelineResponse getTimeline(String sessionId, Instant at);
}
