package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.CoreException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class PickDraft {
    private final Rooms rooms;

    @Transactional
    public RoomTeamMember pick(String code, String teamLeaderId, String playerName) {
        Room room = rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
        RoomPlayerId playerId =
            room.getPlayers().stream()
                .filter(player -> player.getName().equals(playerName))
                .filter(player -> player.getStatus() == PlayerStatus.AVAILABLE)
                .findFirst()
                .map(RoomPlayer::getId)
                .orElseThrow(() -> new IllegalStateException("픽할 선수를 찾을 수 없습니다"));
        RoomTeamMember member = room.pick(new TeamLeaderId(teamLeaderId), playerId);
        rooms.save(room);
        return member;
    }
}
