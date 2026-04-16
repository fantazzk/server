package com.naminhyeok.fantazzk.draft;

import org.jmolecules.ddd.types.Identifier;

record DraftPlayerId(int value) implements Identifier {
    DraftPlayerId {
        if (value < 0) {
            throw new IllegalArgumentException("선수 식별자는 음수일 수 없습니다");
        }
    }
}
