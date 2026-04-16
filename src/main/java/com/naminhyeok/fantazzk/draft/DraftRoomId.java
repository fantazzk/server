package com.naminhyeok.fantazzk.draft;

import java.util.UUID;
import org.jmolecules.ddd.types.Identifier;

record DraftRoomId(String value) implements Identifier {
    DraftRoomId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("드래프트 방 식별자는 비어 있을 수 없습니다");
        }
    }

    static DraftRoomId random() {
        return new DraftRoomId(UUID.randomUUID().toString());
    }
}
