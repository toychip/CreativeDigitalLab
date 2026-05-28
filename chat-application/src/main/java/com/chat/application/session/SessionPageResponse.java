package com.chat.application.session;

import java.util.List;

public record SessionPageResponse(
        List<SessionView> sessions,
        String nextCursor,
        boolean hasNext
) {
    /** 결과는 sessionId 내림차순(최신 먼저). nextCursor = 마지막(가장 과거) sessionId */
    public static SessionPageResponse of(List<SessionEntity> sessions, int limit) {
        List<SessionView> views = sessions.stream().map(SessionView::from).toList();
        if (views.isEmpty()) {
            return new SessionPageResponse(views, null, false);
        }
        return new SessionPageResponse(views, views.getLast().sessionId(), views.size() == limit);
    }
}
