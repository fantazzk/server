package com.naminhyeok.fantazzk.draft;

import java.util.List;

public record DraftRoomState(
    String roomCode,
    DraftRoomStatus status,
    DraftRoomReadiness readiness,
    int teamCount,
    int teamSize,
    DraftOrderStrategy draftOrderStrategy,
    List<Leader> leaders,
    List<Player> players,
    List<Member> members,
    OrderPreview draftOrderPreview,
    Progress progress
) {
    public record Leader(
        String leaderId,
        String nickname,
        Integer draftPosition
    ) {
    }

    public record Player(
        int playerId,
        String name,
        String position,
        int displayOrder,
        boolean assigned
    ) {
    }

    public record Member(
        String leaderId,
        int playerId,
        int assignOrder
    ) {
    }

    public record OrderPreview(List<OrderSlot> slots) {
    }

    public record OrderSlot(
        int draftPosition,
        String leaderId,
        String nickname
    ) {
        static OrderSlot empty(int draftPosition) {
            return new OrderSlot(draftPosition, null, null);
        }

        static OrderSlot from(int draftPosition, DraftLeader leader) {
            return new OrderSlot(draftPosition, leader.id(), leader.nickname());
        }
    }

    public record Progress(
        int currentTurnIndex,
        int currentRound,
        String currentLeaderId,
        List<String> currentRoundLeaderIds
    ) {
    }
}
