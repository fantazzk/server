package com.naminhyeok.fantazzk.room;

public record GameParticipantView(
    String teamLeaderId,
    String nickname,
    Integer draftPosition,
    Integer remainingBudget
) {
    static GameParticipantView from(GameParticipant participant) {
        return new GameParticipantView(
            participant.teamLeaderId().value(),
            participant.nickname(),
            participant.draftPosition(),
            participant.remainingBudget()
        );
    }
}
