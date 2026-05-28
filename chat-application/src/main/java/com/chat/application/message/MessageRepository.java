package com.chat.application.message;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, String> {

    List<MessageEntity> findBySessionIdOrderBySeqDesc(String sessionId, Limit limit);

    /** BEFORE — 커서보다 과거 (seq < cursor, 내림차순) */
    List<MessageEntity> findBySessionIdAndSeqLessThanOrderBySeqDesc(String sessionId, long cursor, Limit limit);

    /** AFTER — 커서보다 최신 (seq > cursor, 오름차순; 호출부에서 reverse) */
    List<MessageEntity> findBySessionIdAndSeqGreaterThanOrderBySeqAsc(String sessionId, long cursor, Limit limit);
}
