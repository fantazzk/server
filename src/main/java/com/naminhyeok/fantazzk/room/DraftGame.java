package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.CoreException;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.Getter;

@Getter
@DiscriminatorValue("DRAFT")
class DraftGame extends Game {
    @Column(name = "current_turn_index")
    private int currentTurnIndex;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "game_draft_member", joinColumns = @JoinColumn(name = "members_game_id"))
    @OrderColumn(name = "member_order")
    private final List<RoomTeamMember> members;

    protected DraftGame() {
        this.currentTurnIndex = 0;
        this.members = new ArrayList<>();
    }

    DraftGame(
        GameId id,
        RoomId roomId,
        String roomCode,
        Instant startedAt,
        GameRules rules,
        List<GameParticipant> participants,
        List<GamePlayer> playerPool,
        int currentTurnIndex
    ) {
        super(id, roomId, roomCode, startedAt, GameStatus.IN_PROGRESS, rules, participants, playerPool);
        this.currentTurnIndex = currentTurnIndex;
        this.members = new ArrayList<>();
    }

    RoomTeamMember pick(TeamLeaderId teamLeaderId, String playerName) {
        if (getStatus() != GameStatus.IN_PROGRESS) {
            throw CoreException.of(RoomErrorType.ROOM_PLAY_REQUIRES_IN_PROGRESS);
        }

        DraftProgress progress = requireCurrentDraftProgress();
        if (!progress.currentLeaderId().equals(teamLeaderId.value())) {
            throw CoreException.of(RoomErrorType.ROOM_PICK_OUT_OF_TURN);
        }

        GamePlayer player =
            mutablePlayerPool().stream()
                .filter(candidate -> candidate.name().equals(playerName))
                .filter(candidate -> isPlayerAvailable(candidate.name()))
                .findFirst()
                .orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_PICK_PLAYER_NOT_AVAILABLE));

        RoomTeamMember member = new RoomTeamMember(teamLeaderId, player.name(), members.size());
        members.add(member);
        currentTurnIndex += 1;

        if (members.size() == getTeamCount() * (getTeamSize() - 1)) {
            changeStatus(GameStatus.COMPLETED);
        }
        return member;
    }

    DraftProgress currentDraftProgress() {
        if (getStatus() != GameStatus.IN_PROGRESS) {
            return null;
        }

        try {
            return DraftProgress.from(getLeaderIdsInDraftOrder(), getDraftOrderStrategy(), currentTurnIndex);
        } catch (IllegalArgumentException ex) {
            throw RoomStateInvalidException.draftLeaderOrderEmpty();
        }
    }

    boolean isPlayerAvailable(String playerName) {
        return mutablePlayerPool().stream().anyMatch(player -> player.name().equals(playerName))
            && members.stream().noneMatch(member -> member.playerName().equals(playerName));
    }

    private DraftProgress requireCurrentDraftProgress() {
        DraftProgress progress = currentDraftProgress();
        if (progress == null) {
            throw RoomStateInvalidException.draftTurnMissing();
        }
        return progress;
    }

    private List<String> getLeaderIdsInDraftOrder() {
        return getParticipantsInDraftOrder().stream().map(participant -> participant.teamLeaderId().value()).toList();
    }

    private List<GameParticipant> getParticipantsInDraftOrder() {
        if (mutableParticipants().isEmpty()) {
            throw RoomStateInvalidException.draftLeaderOrderEmpty();
        }
        GameParticipant participantWithoutDraftPosition =
            mutableParticipants().stream().filter(participant -> participant.draftPosition() == null).findFirst().orElse(null);
        if (participantWithoutDraftPosition != null) {
            throw RoomStateInvalidException.draftPositionMissing(participantWithoutDraftPosition.teamLeaderId());
        }
        return mutableParticipants().stream().sorted(Comparator.comparingInt(GameParticipant::draftPosition)).toList();
    }
}
