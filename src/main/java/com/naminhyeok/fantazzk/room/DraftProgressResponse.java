package com.naminhyeok.fantazzk.room;

import java.util.List;

record DraftProgressResponse(
    int currentTurnIndex,
    int currentRound,
    String currentLeaderId,
    List<String> currentRoundLeaderIds
) {
    static DraftProgressResponse from(Game game) {
        if (!(game instanceof DraftGame draftGame) || draftGame.getStatus() != GameStatus.IN_PROGRESS) {
            return null;
        }
        DraftProgress progress = draftGame.currentDraftProgress();
        return new DraftProgressResponse(
            draftGame.getCurrentTurnIndex(),
            progress.currentRound(),
            progress.currentLeaderId(),
            progress.currentRoundLeaderIds()
        );
    }
}
