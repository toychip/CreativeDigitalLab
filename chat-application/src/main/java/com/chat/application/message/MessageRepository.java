package com.chat.application.message;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, String> {

    List<MessageEntity> findBySessionIdOrderBySeqAsc(String sessionId);
}
