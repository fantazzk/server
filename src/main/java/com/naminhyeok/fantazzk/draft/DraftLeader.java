package com.naminhyeok.fantazzk.draft;

final class DraftLeader {
    private final DraftLeaderId id;
    private final String nickname;
    private Integer draftPosition;

    DraftLeader(String leaderId, String nickname) {
        this.id = new DraftLeaderId(leaderId);
        this.nickname = normalizeNickname(nickname);
    }

    String id() {
        return id.value();
    }

    String nickname() {
        return nickname;
    }

    Integer draftPosition() {
        return draftPosition;
    }

    void assignDraftPosition(int draftPosition) {
        this.draftPosition = draftPosition;
    }

    void clearDraftPosition() {
        this.draftPosition = null;
    }

    private String normalizeNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("팀장 닉네임은 비어 있을 수 없습니다");
        }
        return nickname.trim();
    }
}
