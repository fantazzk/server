package com.naminhyeok.fantazzk.draft;

import org.jmolecules.ddd.types.Identifier;

record DraftLeaderId(String value) implements Identifier {
    DraftLeaderId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("팀장 식별자는 비어 있을 수 없습니다");
        }
    }
}
