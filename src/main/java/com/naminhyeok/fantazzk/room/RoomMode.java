package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.template.TemplateCatalog;
import java.util.Objects;

enum RoomMode {
    AUCTION,
    DRAFT;

    static RoomMode from(TemplateCatalog.Mode mode) {
        return switch (Objects.requireNonNull(mode, "mode must not be null")) {
            case AUCTION -> AUCTION;
            case DRAFT -> DRAFT;
        };
    }
}
