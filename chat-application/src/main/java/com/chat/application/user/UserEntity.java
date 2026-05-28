package com.chat.application.user;

import com.chat.application.common.BaseEntity;
import com.chat.domain.common.IdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserEntity extends BaseEntity {

    // 서버 발급 PK (내부 식별자)
    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    // 클라이언트가 지정하는 식별자 (로그인 아이디). WebSocket ?userId=, senderId 등에서 사용
    @Column(name = "user_id", unique = true, nullable = false, length = 50, updatable = false)
    private String userId;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(length = 50)
    private String status;

    @Column
    private LocalDateTime lastSeenAt;

    public static UserEntity create(String userId, String username) {
        return new UserEntity(
                IdGenerator.generate(),
                userId,
                username,
                null,
                null
        );
    }

    public void touchLastSeen() {
        this.lastSeenAt = LocalDateTime.now();
    }
}
