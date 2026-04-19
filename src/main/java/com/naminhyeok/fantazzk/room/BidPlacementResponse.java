package com.naminhyeok.fantazzk.room;

record BidPlacementResponse(
    String gameId,
    String bidderLeaderId,
    int amount,
    AuctionProgressResponse auctionProgress
) {
    static BidPlacementResponse from(AuctionBid bid, Game game) {
        return new BidPlacementResponse(
            game.getId().gameId().toString(),
            bid.teamLeaderId().value(),
            bid.amount(),
            AuctionProgressResponse.from(game)
        );
    }
}
