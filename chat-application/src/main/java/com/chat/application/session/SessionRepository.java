package com.chat.application.session;

import com.chat.domain.session.SessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface SessionRepository extends JpaRepository<SessionEntity, String> {

    Page<SessionEntity> findByStatus(SessionStatus status, Pageable pageable);

    Page<SessionEntity> findByStartedAtBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);
}
