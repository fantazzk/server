package com.naminhyeok.fantazzk.room.domain;

import com.naminhyeok.fantazzk.template.TemplateCatalog;
import java.util.Objects;

public enum RoomMode {
    AUCTION,
    DRAFT;

    public static RoomMode from(TemplateCatalog.Mode mode) {
        return switch (Objects.requireNonNull(mode, "mode must not be null")) {
            case AUCTION -> AUCTION;
            case DRAFT -> DRAFT;
        };
    }
}
