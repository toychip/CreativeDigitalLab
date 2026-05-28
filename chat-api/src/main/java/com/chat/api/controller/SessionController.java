package com.chat.api.controller;

import com.chat.api.dto.ClientEventRequest;
import com.chat.api.dto.SessionCreateRequest;
import com.chat.api.dto.SessionCreateResponse;
import com.chat.api.dto.UserSessionRequest;
import com.chat.application.session.SessionDetailResponse;
import com.chat.application.session.SessionPageResponse;
import com.chat.application.session.SessionQueryService;
import com.chat.application.session.TimelineResponse;
import com.chat.application.service.ChatEventService;
import com.chat.application.service.command.LifecycleCommand;
import com.chat.application.service.command.UserCommand;
import com.chat.domain.common.IdGenerator;
import com.chat.domain.event.UserEvent;
import com.chat.domain.session.SessionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final ChatEventService chatEventService;
    private final SessionQueryService sessionQueryService;

    @PostMapping
    public SessionCreateResponse createSession(@RequestBody SessionCreateRequest request) {
        String sessionId = IdGenerator.generate();
        chatEventService.createSession(sessionId, request.clientEventId(), request.creatorUserId());
        return new SessionCreateResponse(sessionId);
    }

    /** 세션 참여 */
    @PostMapping("/{sessionId}/join")
    public void joinSession(
            @PathVariable String sessionId,
            @RequestBody UserSessionRequest request) {
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
        chatEventService.appendLifecycle(
                new LifecycleCommand(sessionId, request.clientEventId(), SessionStatus.SUSPENDED));
    }

    /** 세션 종료 */
    @PostMapping("/{sessionId}/end")
    public void endSession(
            @PathVariable String sessionId,
            @RequestBody ClientEventRequest request) {
        chatEventService.appendLifecycle(
                new LifecycleCommand(sessionId, request.clientEventId(), SessionStatus.ENDED));
    }

    // 세션 목록 커서 페이징 (status/기간 필터, 모두 선택적) 커서 = sessionId
    @GetMapping
    public SessionPageResponse listSessions(
            @RequestParam(required = false) SessionStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit) {
        return sessionQueryService.getSessions(status, from, to, cursor, limit);
    }

    // 세션 단건 조회 + 참여 이력 전체(active + 퇴장)
    @GetMapping("/{sessionId}")
    public SessionDetailResponse getSession(@PathVariable String sessionId) {
        return sessionQueryService.getSessionDetail(sessionId);
    }

    // 특정 시점 상태 복원 (이벤트 fold) at 없으면 현재 기준 전체
    @GetMapping("/{sessionId}/timeline")
    public TimelineResponse getTimeline(
            @PathVariable String sessionId,
            @RequestParam(required = false) Instant at) {
        return sessionQueryService.getTimeline(sessionId, at);
    }
}
