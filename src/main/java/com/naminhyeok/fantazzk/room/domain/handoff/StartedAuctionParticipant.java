package com.naminhyeok.fantazzk.room.domain.handoff;

import com.naminhyeok.fantazzk.room.domain.room.RoomMode;
import com.naminhyeok.fantazzk.room.domain.shared.TeamLeaderId;
import java.util.Objects;

public record StartedAuctionParticipant(
    TeamLeaderId teamLeaderId,
    String nickname,
    Integer remainingBudget
) implements StartedGameParticipant {
    public StartedAuctionParticipant {
        Objects.requireNonNull(teamLeaderId, "teamLeaderId must not be null");
        Objects.requireNonNull(nickname, "nickname must not be null");
        Objects.requireNonNull(remainingBudget, "remainingBudget must not be null");
    }

    @Override
    public RoomMode mode() {
        return RoomMode.AUCTION;
    }
}
