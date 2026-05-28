package com.chat.application.service;

import com.chat.domain.event.LifecycleEvent;
import com.chat.domain.event.MessageEvent;
import com.chat.domain.event.UserEvent;
import org.springframework.transaction.annotation.Transactional;

public interface ProjectionService {
    @Transactional
    void handleLifecycle(LifecycleEvent event);

    @Transactional
    void handleUser(UserEvent event);

    @Transactional
    void handleMessage(MessageEvent event);
}
