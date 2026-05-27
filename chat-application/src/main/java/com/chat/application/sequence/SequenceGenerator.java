package com.chat.application.sequence;

import com.chat.application.event.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

// 세션별 시퀀스 발급: Redis INCR, 실패 시 DB MAX(seq)+1 폴백.
//
// 시나리오:
//  1) 정상              → Redis INCR 결과 반환
//  2) Redis 연결 실패    → DataAccessException catch → DB MAX(seq)+1
//  3) Redis 재기동 + 데이터 유실 → AOF + RDB snapshot 으로 카운터 복원
@Slf4j
@Component
@RequiredArgsConstructor
public class SequenceGenerator {

    private static final String KEY_PREFIX = "chat:seq:";

    private final RedisTemplate<String, String> redisTemplate;
    private final EventRepository eventRepository;

    public long nextSeq(String sessionId) {
        String key = KEY_PREFIX + sessionId;
        try {
            Long result = redisTemplate.opsForValue().increment(key);
            if (result != null) {
                return result;
            }
        } catch (DataAccessException e) {
            log.warn("Redis INCR failed for sessionId={}, fallback to DB MAX", sessionId, e);
        }
        return eventRepository.findMaxSeqBySessionId(sessionId).orElse(0L) + 1;
    }
}
