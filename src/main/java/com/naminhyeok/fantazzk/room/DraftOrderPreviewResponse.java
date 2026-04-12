package com.naminhyeok.fantazzk.room;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

record DraftOrderPreviewResponse(List<DraftOrderSlotResponse> slots) {
    static DraftOrderPreviewResponse from(Room room) {
        if (room.getMode() != RoomMode.DRAFT) {
            return null;
        }

        Map<Integer, RoomTeamLeader> leadersByDraftPosition = new HashMap<>();
        room.getLeaders().stream()
            .filter(leader -> leader.getDraftPosition() != null)
            .forEach(leader -> leadersByDraftPosition.put(leader.getDraftPosition(), leader));

        List<DraftOrderSlotResponse> slots =
            IntStream.rangeClosed(1, room.getTeamCount())
                .mapToObj(draftPosition -> {
                    RoomTeamLeader leader = leadersByDraftPosition.get(draftPosition);
                    if (leader == null) {
                        return DraftOrderSlotResponse.empty(draftPosition);
                    }
                    return DraftOrderSlotResponse.from(draftPosition, leader);
                })
                .toList();

        return new DraftOrderPreviewResponse(slots);
    }
}
