package com.chat.application.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface EventRepository extends JpaRepository<EventEntity, Long> {

    /**
     * 재연결 catch-up: 클라가 가진 seq 이후의 이벤트들 시간순 반환.
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
     * 시점 복원: 특정 시각 이전(포함) 이벤트들 seq 순 반환.
     * {@code idx_session_created} 로 범위 스캔, seq 정렬은 deterministic 보장 (created_at 동값 대비).
     */
    @Query("""
        SELECT e FROM EventEntity e
        WHERE e.sessionId = :sessionId AND e.createdAt <= :at
        ORDER BY e.seq
        """)
    List<EventEntity> findEventsUpTo(
            @Param("sessionId") String sessionId,
            @Param("at") Instant at);
}
