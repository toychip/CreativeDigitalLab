package com.chat.api.controller;

import com.chat.api.controller.docs.SessionControllerDocs;
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
public class SessionController implements SessionControllerDocs {

    private final ChatEventService chatEventService;
    private final SessionQueryService sessionQueryService;

    @Override
    @PostMapping
    public SessionCreateResponse createSession(@RequestBody SessionCreateRequest request) {
        String sessionId = IdGenerator.generate();
        chatEventService.createSession(sessionId, request.clientEventId(), request.creatorUserId());
        return new SessionCreateResponse(sessionId);
    }

    @Override
    @PostMapping("/{sessionId}/join")
    public void joinSession(
            @PathVariable String sessionId,
            @RequestBody UserSessionRequest request) {
        chatEventService.appendUser(
                new UserCommand(sessionId, request.clientEventId(), request.userId(), UserEvent.Type.JOINED));
    }

    @Override
    @PostMapping("/{sessionId}/leave")
    public void leaveSession(
            @PathVariable String sessionId,
            @RequestBody UserSessionRequest request) {
        chatEventService.appendUser(
                new UserCommand(sessionId, request.clientEventId(), request.userId(), UserEvent.Type.LEFT));
    }

    @Override
    @PostMapping("/{sessionId}/suspend")
    public void suspendSession(
            @PathVariable String sessionId,
            @RequestBody ClientEventRequest request) {
        chatEventService.appendLifecycle(
                new LifecycleCommand(sessionId, request.clientEventId(), SessionStatus.SUSPENDED));
    }

    @Override
    @PostMapping("/{sessionId}/end")
    public void endSession(
            @PathVariable String sessionId,
            @RequestBody ClientEventRequest request) {
        chatEventService.appendLifecycle(
                new LifecycleCommand(sessionId, request.clientEventId(), SessionStatus.ENDED));
    }

    @Override
    @GetMapping
    public SessionPageResponse listSessions(
            @RequestParam(required = false) SessionStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit) {
        return sessionQueryService.getSessions(status, from, to, cursor, limit);
    }

    @Override
    @GetMapping("/{sessionId}")
    public SessionDetailResponse getSession(@PathVariable String sessionId) {
        return sessionQueryService.getSessionDetail(sessionId);
    }

    @Override
    @GetMapping("/{sessionId}/timeline")
    public TimelineResponse getTimeline(
            @PathVariable String sessionId,
            @RequestParam(required = false) Instant at) {
        return sessionQueryService.getTimeline(sessionId, at);
    }
}
