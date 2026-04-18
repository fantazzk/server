package com.naminhyeok.fantazzk.room.domain.room;

import com.naminhyeok.fantazzk.room.domain.game.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

import com.naminhyeok.fantazzk.template.TemplateCatalog;
import java.util.Objects;

public enum RoomMode {
    AUCTION,
    DRAFT;

    static RoomMode from(TemplateCatalog.Mode mode) {
        return switch (Objects.requireNonNull(mode, "mode must not be null")) {
            case AUCTION -> AUCTION;
            case DRAFT -> DRAFT;
        };
    }
}
