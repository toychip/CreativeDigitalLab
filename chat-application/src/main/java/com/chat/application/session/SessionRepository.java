package com.chat.application.session;

import com.chat.domain.session.SessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface SessionRepository extends JpaRepository<SessionEntity, String> {

    Page<SessionEntity> findByStatus(SessionStatus status, Pageable pageable);

    Page<SessionEntity> findByStartedAtBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);

    /** status, from, to 중 null 인 파라미터는 조건에서 제외 */
    @Query("""
            SELECT s FROM SessionEntity s
            WHERE (:status IS NULL OR s.status = :status)
              AND (:from IS NULL OR s.startedAt >= :from)
              AND (:to   IS NULL OR s.startedAt <= :to)
            """)
    Page<SessionEntity> findWithFilter(
            @Param("status") SessionStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );
}
