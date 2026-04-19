package com.naminhyeok.fantazzk.room;

record DraftPickResponse(
    String gameId,
    String leaderId,
    String pickedPlayerName,
    int assignOrder,
    DraftProgressResponse draftProgress
) {
    static DraftPickResponse from(RosterMember member, Game game) {
        return new DraftPickResponse(
            game.getId().gameId().toString(),
            member.teamLeaderId().value(),
            member.playerName(),
            member.assignOrder(),
            DraftProgressResponse.from(game)
        );
    }
}
