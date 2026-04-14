package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.CoreException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class GetRoomDetails {
    private final Rooms rooms;
    private final Games games;

    @Transactional(readOnly = true)
    RoomDetails get(String code) {
        Room room = rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
        if (room.getStatus() == RoomStatus.STARTED && room.getStartedGameId() == null) {
            throw room.getMode() == RoomMode.AUCTION
                ? RoomStateInvalidException.auctionRoundMissing()
                : RoomStateInvalidException.draftTurnMissing();
        }
        if (room.getStartedGameId() == null) {
            return RoomDetails.from(room);
        }

        Game game =
            games.findById(room.getStartedGameId()).orElseThrow(() -> room.getMode() == RoomMode.AUCTION
                ? RoomStateInvalidException.auctionRoundMissing()
                : RoomStateInvalidException.draftTurnMissing());
        return new RoomDetails(room, game);
    }
}
