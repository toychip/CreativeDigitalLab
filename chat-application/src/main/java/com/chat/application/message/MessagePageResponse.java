package com.chat.application.message;

import java.util.List;

public record MessagePageResponse(
        List<MessageView> messages,
        Long nextCursor,
        Long prevCursor,
        boolean hasNext,
        boolean hasPrev
) {
    /**
     * 조회 결과는 항상 seq 내림차순(최신 먼저)
     * nextCursor = 마지막(가장 과거) seq, prevCursor = 첫(가장 최신) seq
     * hasNext = 요청 limit 만큼 꽉 찼는지, hasPrev = 커서가 있었는지
     */
    public static MessagePageResponse of(List<MessageView> views, Long requestCursor, int limit) {
        if (views.isEmpty()) {
            return new MessagePageResponse(views, null, null, false, requestCursor != null);
        }
        return new MessagePageResponse(
                views,
                views.getLast().seq(),
                views.getFirst().seq(),
                views.size() == limit,
                requestCursor != null
        );
    }
}
