package com.chat.application.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<EventEntity, String> {

    /**
     * 재연결 catch-up: 클라가 가진 seq 이후의 이벤트들 시간순 반환
     * {@code uk_event_session_seq} 인덱스 활용.
     */
    @Query("""
        SELECT e FROM EventEntity e
        WHERE e.sessionId = :sessionId AND e.seq > :afterSeq
        ORDER BY e.seq
        """)
    List<EventEntity> findEventsAfterSeq(
            @Param("sessionId") String sessionId,
            @Param("afterSeq") long afterSeq);

    /**
     * 시점 복원: 특정 시각 이전(포함) 이벤트들 seq 순 반환
     * {@code idx_session_created} 로 범위 스캔, seq 정렬은 deterministic 보장 (created_at 동값 대비)
     */
    @Query("""
        SELECT e FROM EventEntity e
        WHERE e.sessionId = :sessionId AND e.createdAt <= :at
        ORDER BY e.seq
        """)
    List<EventEntity> findEventsUpTo(
            @Param("sessionId") String sessionId,
            @Param("at") Instant at);

    /**
     * SequenceGenerator 의 폴백 초기화용
     * Redis 카운터 유실 시 DB 의 가장 큰 seq 를 조회해서 그 위부터 다시 발급한다.
     * 이벤트가 없는 세션은 Optional.empty() — 호출부에서 0L 로 대체
     */
    @Query("""
        SELECT MAX(e.seq) FROM EventEntity e
        WHERE e.sessionId = :sessionId
        """)
    Optional<Long> findMaxSeqBySessionId(@Param("sessionId") String sessionId);
}
