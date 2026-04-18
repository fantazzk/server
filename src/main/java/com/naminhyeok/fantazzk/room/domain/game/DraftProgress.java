package com.naminhyeok.fantazzk.room.domain.game;

import com.naminhyeok.fantazzk.room.domain.event.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.room.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record DraftProgress(
    int currentTurnIndex,
    int currentRound,
    String currentLeaderId,
    List<String> currentRoundLeaderIds
) {
    public static DraftProgress from(
        List<String> roundOneLeaderIds,
        DraftOrderStrategy strategy,
        int currentTurnIndex
    ) {
        List<String> baseOrder = List.copyOf(roundOneLeaderIds);
        if (baseOrder.isEmpty()) {
            throw new IllegalArgumentException("드래프트 순서가 비어 있습니다");
        }

        int roundSize = baseOrder.size();
        int currentRound = currentTurnIndex / roundSize + 1;
        int turnOffsetInRound = currentTurnIndex % roundSize;
        List<String> currentRoundLeaderIds = currentRoundLeaderIds(baseOrder, strategy, currentRound);

        return new DraftProgress(
            currentTurnIndex,
            currentRound,
            currentRoundLeaderIds.get(turnOffsetInRound),
            currentRoundLeaderIds
        );
    }

    private static List<String> currentRoundLeaderIds(
        List<String> baseOrder,
        DraftOrderStrategy strategy,
        int currentRound
    ) {
        if (strategy == DraftOrderStrategy.FIXED || currentRound % 2 == 1) {
            return baseOrder;
        }

        List<String> reversed = new ArrayList<>(baseOrder);
        Collections.reverse(reversed);
        return List.copyOf(reversed);
    }
}
