package com.naminhyeok.fantazzk.room;

import java.util.List;

record GameRosterProjection(
    List<GameParticipantResponse> participants,
    List<GameMemberResponse> roster
) {
    static GameRosterProjection from(Game game) {
        return new GameRosterProjection(
            game.getParticipants().stream().map(GameParticipantResponse::from).toList(),
            GameDetailResponse.rosterOf(game)
        );
    }
}
