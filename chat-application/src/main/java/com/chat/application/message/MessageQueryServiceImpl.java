package com.chat.application.message;

import com.chat.application.session.SessionRepository;
import com.chat.domain.exception.CdlException;
import com.chat.domain.exception.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageQueryServiceImpl implements MessageQueryService {

    private static final int MAX_LIMIT = 100;

    private final MessageRepository messageRepository;
    private final SessionRepository sessionRepository;

    @Override
    @Transactional(readOnly = true)
    public MessagePageResponse getMessagesByCursor(String sessionId, Long cursor, int limit, MessageDirection direction) {
        if (!sessionRepository.existsById(sessionId)) {
            throw new CdlException(ExceptionCode.SESSION_NOT_FOUND);
        }
        int capped = Math.max(1, Math.min(limit, MAX_LIMIT));
        Limit pageLimit = Limit.of(capped);

        List<MessageEntity> messages = fetch(sessionId, cursor, direction, pageLimit);
        List<MessageView> views = messages.stream().map(MessageView::from).toList();

        return MessagePageResponse.of(views, cursor, capped);
    }

    private List<MessageEntity> fetch(String sessionId, Long cursor, MessageDirection direction, Limit limit) {
        if (cursor == null) {
            return messageRepository.findBySessionIdOrderBySeqDesc(sessionId, limit);
        }
        if (direction == MessageDirection.BEFORE) {
            return messageRepository.findBySessionIdAndSeqLessThanOrderBySeqDesc(sessionId, cursor, limit);
        }
        List<MessageEntity> ascending =
                messageRepository.findBySessionIdAndSeqGreaterThanOrderBySeqAsc(sessionId, cursor, limit);
        List<MessageEntity> reversed = new ArrayList<>(ascending);
        Collections.reverse(reversed);
        return reversed;
    }
}
