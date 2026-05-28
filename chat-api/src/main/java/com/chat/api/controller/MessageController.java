package com.chat.api.controller;

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
public class MessageController {

    private final MessageQueryService messageQueryService;

    /**
     * 메시지 목록 커서 페이징 (read model 직접 조회)
     * cursor 없으면 최신부터, BEFORE 는 과거 방향, AFTER 는 최신 방향. 커서 값은 seq
     */
    @GetMapping("/cursor")
    public MessagePageResponse getMessages(
            @PathVariable String sessionId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "BEFORE") MessageDirection direction) {
        return messageQueryService.getMessagesByCursor(sessionId, cursor, limit, direction);
    }
}
