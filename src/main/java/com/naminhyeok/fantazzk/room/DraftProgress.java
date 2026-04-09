package com.naminhyeok.fantazzk.room;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

record DraftProgress(
    int currentTurnIndex,
    int currentRound,
    String currentLeaderId,
    List<String> currentRoundLeaderIds
) {
    static DraftProgress from(
        List<String> roundOneLeaderIds,
        RoomTemplateSpec.DraftOrderStrategy strategy,
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
        RoomTemplateSpec.DraftOrderStrategy strategy,
        int currentRound
    ) {
        if (strategy == RoomTemplateSpec.DraftOrderStrategy.FIXED || currentRound % 2 == 1) {
            return baseOrder;
        }

        List<String> reversed = new ArrayList<>(baseOrder);
        Collections.reverse(reversed);
        return List.copyOf(reversed);
    }
}
