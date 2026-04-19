package com.naminhyeok.fantazzk.room.web;

import com.naminhyeok.fantazzk.room.domain.Game;
import com.naminhyeok.fantazzk.room.query.AuctionProgressResponse;
import com.naminhyeok.fantazzk.room.query.GameDetailResponse;
import com.naminhyeok.fantazzk.room.query.GameMemberResponse;
import java.util.List;

public record AuctionProgressUpdateResponse(
    String gameId,
    AuctionProgressResponse auctionProgress,
    List<GameMemberResponse> roster
) {
    public static AuctionProgressUpdateResponse from(Game game) {
        return new AuctionProgressUpdateResponse(
            game.getId().gameId().toString(),
            AuctionProgressResponse.from(game),
            GameDetailResponse.rosterOf(game)
        );
    }
}
