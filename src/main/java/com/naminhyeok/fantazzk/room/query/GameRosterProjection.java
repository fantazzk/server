package com.naminhyeok.fantazzk.room.query;

import com.naminhyeok.fantazzk.room.domain.Game;
import java.util.List;

public record GameRosterProjection(
    List<GameParticipantResponse> participants,
    List<GameMemberResponse> roster
) {
    public static GameRosterProjection from(Game game) {
        return new GameRosterProjection(
            game.getParticipants().stream().map(GameParticipantResponse::from).toList(),
            GameDetailResponse.rosterOf(game)
        );
    }
}
