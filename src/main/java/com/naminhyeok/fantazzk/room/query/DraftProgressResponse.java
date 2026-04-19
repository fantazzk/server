package com.naminhyeok.fantazzk.room.query;

import com.naminhyeok.fantazzk.room.domain.DraftGame;
import com.naminhyeok.fantazzk.room.domain.DraftProgress;
import com.naminhyeok.fantazzk.room.domain.Game;
import com.naminhyeok.fantazzk.room.domain.GameStatus;
import java.util.List;

public record DraftProgressResponse(
    int currentTurnIndex,
    int currentRound,
    String currentLeaderId,
    List<String> currentRoundLeaderIds
) {
    public static DraftProgressResponse from(Game game) {
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
