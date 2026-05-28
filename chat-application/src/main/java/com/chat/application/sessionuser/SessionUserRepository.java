package com.chat.application.sessionuser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionUserRepository extends JpaRepository<SessionUserEntity, Long> {

    List<SessionUserEntity> findBySessionIdAndIsActiveTrue(String sessionId);

    /** 참여 이력 전체 (active + 퇴장), 참여 순 */
    List<SessionUserEntity> findBySessionIdOrderByJoinedAtAsc(String sessionId);

    List<SessionUserEntity> findByUserIdAndIsActiveTrue(String userId);

    Optional<SessionUserEntity> findBySessionIdAndUserIdAndIsActiveTrue(String sessionId, String userId);

    /** isActive 무관하게 조회 — rejoin 시 기존 row 찾기용 */
    Optional<SessionUserEntity> findBySessionIdAndUserId(String sessionId, String userId);

    boolean existsBySessionIdAndUserIdAndIsActiveTrue(String sessionId, String userId);

    @Modifying
    @Query("""
            UPDATE SessionUserEntity su
            SET su.isActive = false, su.leftAt = CURRENT_TIMESTAMP
            WHERE su.sessionId = :sessionId AND su.userId = :userId AND su.isActive = true
            """)
    void leaveSession(String sessionId, String userId);
}
