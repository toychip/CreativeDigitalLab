package com.chat.application.session;

import com.chat.domain.session.SessionStatus;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<SessionEntity, String> {

    /**
     * 커서 페이징 + 필터. status/from/to/cursor 중 null 은 조건 제외
     * 커서 = sessionId(UUID v7) cursor 보다 과거(작은) 세션을 최신순으로
     */
    @Query("""
            SELECT s FROM SessionEntity s
            WHERE (:status IS NULL OR s.status = :status)
              AND (:from IS NULL OR s.startedAt >= :from)
              AND (:to   IS NULL OR s.startedAt <= :to)
              AND (:cursor IS NULL OR s.sessionId < :cursor)
            ORDER BY s.sessionId DESC
            """)
    List<SessionEntity> findWithCursor(
            @Param("status") SessionStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("cursor") String cursor,
            Limit limit
    );
}
