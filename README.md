# CreativeDigitalLab 과제 레포지토리

## API 문서

- **REST API**: 앱 실행 후 Swagger UI — `http://localhost:8080/swagger-ui.html` (OpenAPI JSON: `http://localhost:8080/v3/api-docs`)
- **WebSocket**: OpenAPI 로 기술할 수 없어 아래에 별도 정리

---

## WebSocket 실시간 메시지 API

### 연결

```
ws://<host>/ws/chat?userId=<userId>
```

- 핸드셰이크 시 `userId` 쿼리 파라미터로 사용자를 식별한다 (인증은 과제 비목표 — 토큰 없음). `userId` 누락 시 핸드셰이크 거부.
- 연결 직후, 해당 사용자가 참여 중인 활성 세션을 자동 구독한다.
- 멀티 인스턴스: 어느 노드에 연결돼도 Redis Pub/Sub fanout 으로 같은 세션 메시지를 수신한다 (그래서 sticky session 이 불필요).

### 클라이언트 → 서버 (인바운드)

세 가지 `type` 을 보낸다. 모두 `clientEventId` 로 멱등 처리되어, 네트워크 재전송 시 중복 저장이 차단된다.

메시지 전송
```json
{ "type": "SEND_MESSAGE", "sessionId": "<sessionId>", "clientEventId": "<uuid>", "content": "안녕하세요" }
```

메시지 수정
```json
{ "type": "EDIT_MESSAGE", "sessionId": "<sessionId>", "clientEventId": "<uuid>", "messageId": "<messageId>", "content": "수정본" }
```

메시지 삭제
```json
{ "type": "DELETE_MESSAGE", "sessionId": "<sessionId>", "clientEventId": "<uuid>", "messageId": "<messageId>" }
```

### 서버 → 클라이언트 (아웃바운드)

메시지 이벤트 broadcast (`ChatMessageResponse`)
```json
{
  "messageStatus": "SENT",
  "sessionId": "<sessionId>",
  "seq": 42,
  "createdAt": "2026-05-28T01:00:00Z",
  "clientEventId": "<uuid>",
  "senderId": "<userId>",
  "messageId": "<messageId>",
  "content": "안녕하세요"
}
```

- `messageStatus`: `SENT` | `EDITED` | `DELETED`
- `seq`: 세션 내 서버 발급 순번. 순서 보장·정렬·재연결 catch-up 의 기준.

에러 (`ErrorMessage`)
```json
{ "code": "INVALID_MESSAGE_FORMAT", "message": "메시지 형식이 올바르지 않습니다", "detail": null, "clientEventId": "<uuid>" }
```

### 중복 / 순서 처리 기준

- **중복**: 동일 `(sessionId, clientEventId, eventType)` 는 events 테이블 UNIQUE 제약으로 차단 (재전송 안전).
- **순서**: 서버가 세션별 `seq`(Redis INCR)를 발급. 조회·시점 복원은 `seq` 순 정렬로 fold 하여 결정적(deterministic)으로 복원한다.

### 빠른 테스트 (websocat 예시)

```bash
# 1) 회원 등록 (REST)
curl -X POST localhost:8080/users -H 'Content-Type: application/json' \
  -d '{"userId":"alice","username":"Alice"}'

# 2) 세션 생성 (REST) → 응답의 sessionId 확보
curl -X POST localhost:8080/sessions -H 'Content-Type: application/json' \
  -d '{"creatorUserId":"alice","clientEventId":"create-1"}'

# 3) WebSocket 연결 후 메시지 전송
websocat "ws://localhost:8080/ws/chat?userId=alice"
# (연결되면 아래 JSON 한 줄을 입력)
{"type":"SEND_MESSAGE","sessionId":"<위 sessionId>","clientEventId":"msg-1","content":"hello"}
```
