package com.chat.api.controller;

import com.chat.api.controller.docs.MessageControllerDocs;
import com.chat.application.message.MessageDirection;
import com.chat.application.message.MessagePageResponse;
import com.chat.application.message.MessageQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sessions/{sessionId}/messages")
@RequiredArgsConstructor
public class MessageController implements MessageControllerDocs {

    private final MessageQueryService messageQueryService;

    @Override
    @GetMapping("/cursor")
    public MessagePageResponse getMessages(
            @PathVariable String sessionId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "BEFORE") MessageDirection direction) {
        return messageQueryService.getMessagesByCursor(sessionId, cursor, limit, direction);
    }
}
