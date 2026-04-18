package com.naminhyeok.fantazzk.room.application.support;

import com.naminhyeok.fantazzk.room.domain.game.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.repository.*;
import com.naminhyeok.fantazzk.room.domain.room.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateRoomAttempt {
    private final Rooms rooms;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Room save(Room room) {
        return rooms.saveAndFlush(room);
    }
}
