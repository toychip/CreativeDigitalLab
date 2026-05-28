package com.chat.application.user;

import com.chat.application.common.BaseEntity;
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

    @Id
    @Column(name = "user_id", length = 36, nullable = false, updatable = false)
    private String userId;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(length = 50)
    private String status;

    @Column
    private LocalDateTime lastSeenAt;

    public static UserEntity create(String userId, String username) {
        return new UserEntity(
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
