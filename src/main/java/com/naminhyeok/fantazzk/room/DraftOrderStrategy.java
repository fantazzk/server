package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.template.TemplateCatalog;

enum DraftOrderStrategy {
    SNAKE,
    FIXED;

    static DraftOrderStrategy from(TemplateCatalog.DraftOrderStrategy strategy) {
        if (strategy == null) {
            return null;
        }
        return switch (strategy) {
            case SNAKE -> SNAKE;
            case FIXED -> FIXED;
        };
    }
}
