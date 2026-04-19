package com.naminhyeok.fantazzk.room;

import java.util.List;

record AuctionProgressUpdateResponse(
    String gameId,
    AuctionProgressResponse auctionProgress,
    List<GameMemberResponse> roster
) {
    static AuctionProgressUpdateResponse from(Game game) {
        return new AuctionProgressUpdateResponse(
            game.getId().gameId().toString(),
            AuctionProgressResponse.from(game),
            GameDetailResponse.rosterOf(game)
        );
    }
}
