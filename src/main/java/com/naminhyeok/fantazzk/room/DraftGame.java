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
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "game_participant", joinColumns = @JoinColumn(name = "participants_game_id"))
    @OrderColumn(name = "participant_order")
    private final List<DraftParticipant> participants;
    @Column(name = "current_turn_index")
    private int currentTurnIndex;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "game_draft_member", joinColumns = @JoinColumn(name = "members_game_id"))
    @OrderColumn(name = "member_order")
    private final List<RosterMember> members;

    protected DraftGame() {
        this.participants = new ArrayList<>();
        this.currentTurnIndex = 0;
        this.members = new ArrayList<>();
    }

    DraftGame(
        GameId id,
        RoomId roomId,
        String roomCode,
        Instant startedAt,
        GameRules rules,
        List<DraftParticipant> participants,
        List<GamePlayer> playerPool,
        int currentTurnIndex
    ) {
        super(id, roomId, roomCode, startedAt, GameStatus.IN_PROGRESS, rules, playerPool);
        this.participants = new ArrayList<>(participants);
        this.currentTurnIndex = currentTurnIndex;
        this.members = new ArrayList<>();
    }

    @Override
    GameRules getRules() {
        return GameRules.draft(
            getTeamCount(),
            getTeamSize(),
            getPickBanTime(),
            getDraftOrderStrategy()
        );
    }

    @Override
    List<GameParticipant> getParticipants() {
        return participants.stream().map(GameParticipant.class::cast).toList();
    }

    RosterMember pick(TeamLeaderId teamLeaderId, String playerName) {
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

        RosterMember member = new RosterMember(teamLeaderId, player.name(), members.size());
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
            return DraftProgress.from(getLeaderIdsInDraftOrder(), getRules().draftRules().draftOrderStrategy(), currentTurnIndex);
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

    private List<DraftParticipant> getParticipantsInDraftOrder() {
        if (mutableParticipants().isEmpty()) {
            throw RoomStateInvalidException.draftLeaderOrderEmpty();
        }
        DraftParticipant participantWithoutDraftPosition =
            mutableParticipants().stream().filter(participant -> participant.draftPosition() == null).findFirst().orElse(null);
        if (participantWithoutDraftPosition != null) {
            throw RoomStateInvalidException.draftPositionMissing(participantWithoutDraftPosition.teamLeaderId());
        }
        return mutableParticipants().stream().sorted(Comparator.comparingInt(participant -> participant.draftState().draftPosition())).toList();
    }

    private List<DraftParticipant> mutableParticipants() {
        return participants;
    }
}
