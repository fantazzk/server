package com.naminhyeok.fantazzk.room.application;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.RoomErrorType;
import com.naminhyeok.fantazzk.room.repository.Rooms;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettleAuction {
    private final SettleAuctionAttempt settleAuctionAttempt;
    private final Rooms rooms;

    public Room settleIfDue(String code) {
        try {
            return settleAuctionAttempt.settleIfDue(code);
        } catch (OptimisticLockingFailureException ex) {
            return rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
        }
    }
}
