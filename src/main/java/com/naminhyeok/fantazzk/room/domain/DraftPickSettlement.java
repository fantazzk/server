package com.naminhyeok.fantazzk.room.domain;

public final class DraftPickSettlement {
    private final int nextTurnIndex;
    private final boolean completed;

    public DraftPickSettlement(int nextTurnIndex, boolean completed) {
        this.nextTurnIndex = nextTurnIndex;
        this.completed = completed;
    }

    public int getNextTurnIndex() {
        return nextTurnIndex;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean getCompleted() {
        return completed;
    }
}
