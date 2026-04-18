package com.naminhyeok.fantazzk.room.domain.handoff;

import com.naminhyeok.fantazzk.room.domain.room.RoomMode;
import com.naminhyeok.fantazzk.room.domain.shared.TeamLeaderId;
import java.util.Objects;

public record StartedDraftParticipant(
    TeamLeaderId teamLeaderId,
    String nickname,
    Integer draftPosition
) implements StartedGameParticipant {
    public StartedDraftParticipant {
        Objects.requireNonNull(teamLeaderId, "teamLeaderId must not be null");
        Objects.requireNonNull(nickname, "nickname must not be null");
        Objects.requireNonNull(draftPosition, "draftPosition must not be null");
    }

    @Override
    public RoomMode mode() {
        return RoomMode.DRAFT;
    }
}
