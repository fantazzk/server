package com.naminhyeok.fantazzk.room.domain.room;

import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

public enum RoomStartReadiness {
    WAITING_FOR_LEADERS,
    WAITING_FOR_DRAFT_POSITIONS,
    STARTABLE,
    NOT_WAITING
}
