package com.naminhyeok.fantazzk.draft;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@org.jmolecules.ddd.annotation.Service
@RequiredArgsConstructor
public class DraftRoomLifecycle {
    private final DraftRooms draftRooms;

    @Transactional
    public DraftRoomState create(
        String roomCode,
        int teamCount,
        int teamSize,
        DraftOrderStrategy draftOrderStrategy,
        List<DraftPlayerSpec> players
    ) {
        DraftRoomId roomId = new DraftRoomId(roomCode);
        if (draftRooms.findById(roomId).isPresent()) {
            throw new IllegalStateException("드래프트 방이 이미 존재합니다: " + roomCode);
        }

        DraftRoom draftRoom = DraftRoom.create(roomId, teamCount, teamSize, draftOrderStrategy, players);
        draftRooms.save(draftRoom);
        return draftRoom.snapshot();
    }

    @Transactional
    public DraftRoomState addLeader(String roomCode, String leaderId, String nickname) {
        DraftRoom draftRoom = load(roomCode);
        draftRoom.addLeader(leaderId, nickname);
        draftRooms.save(draftRoom);
        return draftRoom.snapshot();
    }

    @Transactional
    public DraftRoomState selectDraftPosition(String roomCode, String leaderId, int draftPosition) {
        DraftRoom draftRoom = load(roomCode);
        draftRoom.selectDraftPosition(leaderId, draftPosition);
        draftRooms.save(draftRoom);
        return draftRoom.snapshot();
    }

    @Transactional
    public DraftRoomState clearDraftPosition(String roomCode, String leaderId) {
        DraftRoom draftRoom = load(roomCode);
        draftRoom.clearDraftPosition(leaderId);
        draftRooms.save(draftRoom);
        return draftRoom.snapshot();
    }

    @Transactional
    public DraftRoomState start(String roomCode) {
        DraftRoom draftRoom = load(roomCode);
        draftRoom.start();
        draftRooms.save(draftRoom);
        return draftRoom.snapshot();
    }

    private DraftRoom load(String roomCode) {
        return draftRooms.findById(new DraftRoomId(roomCode))
            .orElseThrow(() -> new IllegalArgumentException("드래프트 방을 찾을 수 없습니다: " + roomCode));
    }
}
