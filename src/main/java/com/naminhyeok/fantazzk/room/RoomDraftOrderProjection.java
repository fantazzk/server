package com.naminhyeok.fantazzk.room;

record RoomDraftOrderProjection(
    String startReadiness,
    DraftOrderPreviewResponse draftOrder
) {
    static RoomDraftOrderProjection from(Room room) {
        return new RoomDraftOrderProjection(room.getStartReadiness().name(), DraftOrderPreviewResponse.from(room));
    }
}
