package com.naminhyeok.fantazzk.room.domain.shared;

import com.naminhyeok.fantazzk.template.TemplateCatalog;

public enum DraftOrderStrategy {
    SNAKE,
    FIXED;

    public static DraftOrderStrategy from(TemplateCatalog.DraftOrderStrategy strategy) {
        if (strategy == null) {
            return null;
        }
        return switch (strategy) {
            case SNAKE -> SNAKE;
            case FIXED -> FIXED;
        };
    }
}
