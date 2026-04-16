package com.naminhyeok.fantazzk.draft;

public record DraftPlayerSpec(
    int playerId,
    String name,
    String position,
    int displayOrder
) {
    public DraftPlayerSpec {
        if (playerId < 0) {
            throw new IllegalArgumentException("선수 식별자는 음수일 수 없습니다");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("선수 이름은 비어 있을 수 없습니다");
        }
        if (position == null || position.isBlank()) {
            throw new IllegalArgumentException("선수 포지션은 비어 있을 수 없습니다");
        }
        if (displayOrder < 0) {
            throw new IllegalArgumentException("선수 표시 순서는 음수일 수 없습니다");
        }
    }
}
