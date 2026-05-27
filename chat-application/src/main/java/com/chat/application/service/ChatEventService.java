package com.chat.application.service;

import com.chat.application.service.command.LifecycleCommand;
import com.chat.application.service.command.MessageCommand;
import com.chat.application.service.command.UserCommand;
import com.chat.domain.event.LifecycleEvent;
import com.chat.domain.event.MessageEvent;
import com.chat.domain.event.UserEvent;

import java.util.Optional;

public interface ChatEventService {

    Optional<LifecycleEvent> appendLifecycle(LifecycleCommand command);

    Optional<UserEvent> appendUser(UserCommand command);

    Optional<MessageEvent> appendMessage(MessageCommand command);
}
