package com.chat.application.sessionuser;

import com.chat.application.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "session_users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_session_user", columnNames = {"session_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_session_user_session_id", columnList = "session_id"),
                @Index(name = "idx_session_user_user_id", columnList = "user_id"),
                @Index(name = "idx_session_user_active", columnList = "is_active")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SessionUserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", length = 36, nullable = false)
    private String sessionId;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MemberRole role;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    @Column
    private LocalDateTime leftAt;

    public static SessionUserEntity join(String sessionId, String userId, MemberRole role) {
        return new SessionUserEntity(
                null,
                sessionId,
                userId,
                role,
                true,
                LocalDateTime.now(),
                null
        );
    }

    /** leave 후 재참여: 기존 row 재활성화 */
    public void rejoin() {
        this.isActive = true;
        this.leftAt = null;
        this.joinedAt = LocalDateTime.now();
    }
}
