package com.chat.application.service;

import com.chat.application.service.command.LifecycleCommand;
import com.chat.application.service.command.MessageCommand;
import com.chat.application.service.command.UserCommand;
import com.chat.domain.event.LifecycleEvent;
import com.chat.domain.event.MessageEvent;
import com.chat.domain.event.UserEvent;

import java.util.Optional;

public interface ChatEventService {

    // 세션 생성: LifecycleEvent(ACTIVE) + 생성자 UserEvent(JOINED) 를 한 트랜잭션으로 원자 발행.
    void createSession(String sessionId, String clientEventId, String creatorUserId);

    Optional<LifecycleEvent> appendLifecycle(LifecycleCommand command);

    Optional<UserEvent> appendUser(UserCommand command);

    Optional<MessageEvent> appendMessage(MessageCommand command);
}
