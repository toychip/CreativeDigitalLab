-- ============================================================
-- chat-cdl :: 이벤트 영속 스키마
-- ============================================================
-- Event Sourcing 의 진실의 원천. append-only.
-- 도메인의 ChatEvent (LifecycleEvent / UserEvent / MessageEvent) 가 모두 이 테이블에 저장.
--
-- PK 정책
-- ─────────────────────────────────────────────────────────────
-- event_id (UUID v7) 를 PK 로 사용. 별도 surrogate id 없음.
-- UUID v7 은 시간 정렬 → clustered index 가 자연스럽게 시간순 → locality 양호.
-- 도메인이 식별자 발급을 책임 (chat-domain 의 IdGenerator).
--
-- 인덱스 — 4가지 우려사항 + 시점 복원 핫패스
-- ─────────────────────────────────────────────────────────────
-- ① clientEventId (멱등성 키)   — 컬럼으로 보유
-- ② cross-server dedup          — uk_event_session_client UNIQUE
-- ③ serverId (self-echo)         — DB 아님 / 런타임 broadcast 레이어 처리
-- ④ seq (서버 발급 순서)          — uk_event_session_seq UNIQUE + 정렬용
-- 추가) 시점 복원                 — idx_session_created (session_id, created_at)
-- ============================================================

CREATE TABLE IF NOT EXISTS event (
    event_id        CHAR(36)    NOT NULL COMMENT 'UUID v7. PK. 도메인이 발급',
    session_id      CHAR(36)    NOT NULL COMMENT '세션 식별자',
    client_event_id CHAR(36)    NOT NULL COMMENT '클라이언트 멱등성 키. 재전송 시 동일',
    seq             BIGINT      NOT NULL COMMENT '서버 발급 시퀀스 (Redis INCR per session)',
    event_type      VARCHAR(20) NOT NULL COMMENT 'LIFECYCLE | USER | MESSAGE',
    payload         JSON        NOT NULL COMMENT 'type-specific 페이로드 (직렬화된 도메인 이벤트)',
    created_at      DATETIME(6) NOT NULL COMMENT '서버 수신 시각',

    PRIMARY KEY (event_id),

    -- ② cross-server 멱등성 — 동일 (session, clientEventId) 두 번 INSERT 차단
    UNIQUE KEY uk_event_session_client (session_id, client_event_id),

    -- ④ 순서 보장 — 동일 (session, seq) 두 번 INSERT 차단 + 정렬/Resume 핫패스
    UNIQUE KEY uk_event_session_seq (session_id, seq),

    -- 시점 복원 — GET /sessions/{id}/timeline?at=... 의 범위 스캔
    KEY idx_session_created (session_id, created_at)
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4
COLLATE = utf8mb4_unicode_ci
COMMENT = '이벤트 소싱 append-only 저장소';
