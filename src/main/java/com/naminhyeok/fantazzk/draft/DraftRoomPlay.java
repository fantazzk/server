package com.naminhyeok.fantazzk.draft;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@org.jmolecules.ddd.annotation.Service
@RequiredArgsConstructor
public class DraftRoomPlay {
    private final DraftRooms draftRooms;

    @Transactional
    public DraftRoomState pick(String roomCode, String leaderId, int playerId) {
        DraftRoom draftRoom = load(roomCode);
        draftRoom.pick(leaderId, playerId);
        draftRooms.save(draftRoom);
        return draftRoom.snapshot();
    }

    private DraftRoom load(String roomCode) {
        return draftRooms.findById(new DraftRoomId(roomCode))
            .orElseThrow(() -> new IllegalArgumentException("드래프트 방을 찾을 수 없습니다: " + roomCode));
    }
}
