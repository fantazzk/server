package com.naminhyeok.fantazzk.room;

record GameParticipantResponse(
    String teamLeaderId,
    String nickname,
    Integer draftPosition,
    Integer remainingBudget
) {
    static GameParticipantResponse from(GameParticipant participant) {
        return new GameParticipantResponse(
            participant.teamLeaderId().value(),
            participant.nickname(),
            participant.draftPosition(),
            participant.remainingBudget()
        );
    }
}
