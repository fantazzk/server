package com.naminhyeok.fantazzk.draft;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@org.jmolecules.ddd.annotation.Service
@RequiredArgsConstructor
public class DraftRoomStateReader {
    private final DraftRooms draftRooms;

    @Transactional(readOnly = true)
    public DraftRoomState read(String roomCode) {
        return draftRooms.findById(new DraftRoomId(roomCode))
            .map(DraftRoom::snapshot)
            .orElseThrow(() -> new IllegalArgumentException("드래프트 방을 찾을 수 없습니다: " + roomCode));
    }
}
