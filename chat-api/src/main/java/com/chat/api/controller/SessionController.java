package com.chat.api.controller;

import com.chat.api.dto.ClientEventRequest;
import com.chat.api.dto.SessionCreateRequest;
import com.chat.api.dto.SessionCreateResponse;
import com.chat.api.dto.SessionSummary;
import com.chat.api.dto.TimelineResponse;
import com.chat.api.dto.UserSessionRequest;
import com.chat.api.exception.ApiException;
import com.chat.application.event.EventEntity;
import com.chat.application.event.EventRepository;
import com.chat.application.session.SessionExceptionCode;
import com.chat.application.session.SessionRepository;
import com.chat.application.service.ChatEventService;
import com.chat.application.service.command.LifecycleCommand;
import com.chat.application.service.command.UserCommand;
import com.chat.domain.common.IdGenerator;
import com.chat.domain.event.ChatEvent;
import com.chat.domain.event.UserEvent;
import com.chat.domain.session.ChatSession;
import com.chat.domain.session.SessionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final ChatEventService chatEventService;
    private final SessionRepository sessionRepository;
    private final EventRepository eventRepository;

    /** 세션 생성: LifecycleEvent(ACTIVE) + UserEvent(JOINED) 발행 */
    @PostMapping
    public SessionCreateResponse createSession(@RequestBody SessionCreateRequest request) {
        String sessionId = IdGenerator.generate();

        chatEventService.appendLifecycle(
                new LifecycleCommand(sessionId, request.clientEventId() + "-lifecycle", SessionStatus.ACTIVE));

        chatEventService.appendUser(
                new UserCommand(sessionId, request.clientEventId() + "-user",
                        request.creatorUserId(), UserEvent.Type.JOINED));

        return new SessionCreateResponse(sessionId);
    }

    /** 세션 참여 */
    @PostMapping("/{sessionId}/join")
    public void joinSession(
            @PathVariable String sessionId,
            @RequestBody UserSessionRequest request) {
        validateNotEnded(sessionId);
        chatEventService.appendUser(
                new UserCommand(sessionId, request.clientEventId(), request.userId(), UserEvent.Type.JOINED));
    }

    /** 세션 퇴장 */
    @PostMapping("/{sessionId}/leave")
    public void leaveSession(
            @PathVariable String sessionId,
            @RequestBody UserSessionRequest request) {
        chatEventService.appendUser(
                new UserCommand(sessionId, request.clientEventId(), request.userId(), UserEvent.Type.LEFT));
    }

    /** 세션 중단 */
    @PostMapping("/{sessionId}/suspend")
    public void suspendSession(
            @PathVariable String sessionId,
            @RequestBody ClientEventRequest request) {
        validateNotEnded(sessionId);
        chatEventService.appendLifecycle(
                new LifecycleCommand(sessionId, request.clientEventId(), SessionStatus.SUSPENDED));
    }

    /** 세션 종료 */
    @PostMapping("/{sessionId}/end")
    public void endSession(
            @PathVariable String sessionId,
            @RequestBody ClientEventRequest request) {
        validateNotEnded(sessionId);
        chatEventService.appendLifecycle(
                new LifecycleCommand(sessionId, request.clientEventId(), SessionStatus.ENDED));
    }

    /** 세션 목록 조회 (status, from, to 조합 필터, 모두 선택적) */
    @GetMapping
    public Page<SessionSummary> listSessions(
            @RequestParam(required = false) SessionStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 20) Pageable pageable) {
        return sessionRepository.findWithFilter(status, from, to, pageable)
                .map(SessionSummary::from);
    }

    /**
     * 특정 시점 상태 복원
     * at 없으면 현재 기준 전체 이벤트 fold
     */
    @GetMapping("/{sessionId}/timeline")
    public TimelineResponse getTimeline(
            @PathVariable String sessionId,
            @RequestParam(required = false) Instant at) {
        List<ChatEvent> events;
        if (at != null) {
            events = eventRepository.findEventsUpTo(sessionId, at).stream()
                    .map(EventEntity::toDomain)
                    .toList();
        } else {
            events = eventRepository.findAllBySessionId(sessionId).stream()
                    .map(EventEntity::toDomain)
                    .toList();
        }
        ChatSession session = ChatSession.loadFromEvents(events);
        return TimelineResponse.from(session);
    }

    /** ENDED 세션에 대한 상태 변경 차단 */
    private void validateNotEnded(String sessionId) {
        sessionRepository.findById(sessionId).ifPresent(entity -> {
            if (entity.getStatus() == SessionStatus.ENDED) {
                throw new ApiException(HttpStatus.CONFLICT, SessionExceptionCode.SESSION_ALREADY_ENDED);
            }
        });
    }
}
