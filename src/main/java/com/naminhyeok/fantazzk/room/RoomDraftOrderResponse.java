package com.naminhyeok.fantazzk.room;

record RoomDraftOrderResponse(
    String roomCode,
    String startReadiness,
    DraftOrderPreviewResponse draftOrder
) {
    static RoomDraftOrderResponse from(Room room) {
        return new RoomDraftOrderResponse(
            room.getCode(),
            room.getStartReadiness().name(),
            DraftOrderPreviewResponse.from(room)
        );
    }
}
