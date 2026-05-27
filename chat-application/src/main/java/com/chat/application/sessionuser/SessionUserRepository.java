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

    List<SessionUserEntity> findByUserIdAndIsActiveTrue(String userId);

    Optional<SessionUserEntity> findBySessionIdAndUserIdAndIsActiveTrue(String sessionId, String userId);

    /** isActive 무관하게 조회 — rejoin 시 기존 row 찾기용 */
    Optional<SessionUserEntity> findBySessionIdAndUserId(String sessionId, String userId);

    boolean existsBySessionIdAndUserIdAndIsActiveTrue(String sessionId, String userId);

    @Query("SELECT COUNT(su) FROM SessionUserEntity su WHERE su.sessionId = :sessionId AND su.isActive = true")
    long countActiveMembersInSession(String sessionId);

    @Modifying
    @Query("""
            UPDATE SessionUserEntity su
            SET su.isActive = false, su.leftAt = CURRENT_TIMESTAMP
            WHERE su.sessionId = :sessionId AND su.userId = :userId
            """)
    void leaveSession(String sessionId, String userId);
}
