package com.naminhyeok.fantazzk.draft;

final class DraftPlayer {
    private final DraftPlayerId id;
    private final String name;
    private final String position;
    private final int displayOrder;
    private DraftPlayerStatus status;

    DraftPlayer(DraftPlayerSpec spec) {
        this.id = new DraftPlayerId(spec.playerId());
        this.name = spec.name().trim();
        this.position = spec.position().trim();
        this.displayOrder = spec.displayOrder();
        this.status = DraftPlayerStatus.AVAILABLE;
    }

    DraftPlayer(int playerId, String name, String position, int displayOrder, DraftPlayerStatus status) {
        this.id = new DraftPlayerId(playerId);
        this.name = name.trim();
        this.position = position.trim();
        this.displayOrder = displayOrder;
        this.status = status;
    }

    DraftPlayerId id() {
        return id;
    }

    String name() {
        return name;
    }

    String position() {
        return position;
    }

    int displayOrder() {
        return displayOrder;
    }

    DraftPlayerStatus status() {
        return status;
    }

    void assign() {
        status = DraftPlayerStatus.ASSIGNED;
    }
}
