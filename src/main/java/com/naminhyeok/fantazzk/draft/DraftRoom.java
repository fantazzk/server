package com.naminhyeok.fantazzk.draft;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

class DraftRoom {
    private final DraftRoomId id;
    private final int teamCount;
    private final int teamSize;
    private final DraftOrderStrategy draftOrderStrategy;
    private final List<DraftLeader> leaders;
    private final List<DraftPlayer> players;
    private final List<DraftMember> members;
    private DraftRoomStatus status;
    private Integer currentTurnIndex;

    private DraftRoom(
        DraftRoomId id,
        int teamCount,
        int teamSize,
        DraftOrderStrategy draftOrderStrategy,
        List<DraftPlayer> players
    ) {
        if (teamCount <= 0) {
            throw new IllegalArgumentException("팀 수는 0보다 커야 합니다");
        }
        if (teamSize <= 0) {
            throw new IllegalArgumentException("팀 크기는 0보다 커야 합니다");
        }
        this.id = id;
        this.teamCount = teamCount;
        this.teamSize = teamSize;
        this.draftOrderStrategy = Objects.requireNonNull(draftOrderStrategy, "드래프트 순서 전략은 필수입니다");
        this.leaders = new ArrayList<>();
        this.players = new ArrayList<>(players);
        this.members = new ArrayList<>();
        this.status = DraftRoomStatus.WAITING;
        this.currentTurnIndex = null;
    }

    static DraftRoom create(
        DraftRoomId id,
        int teamCount,
        int teamSize,
        DraftOrderStrategy draftOrderStrategy,
        List<DraftPlayerSpec> playerSpecs
    ) {
        int requiredPlayerCount = teamCount * (teamSize - 1);
        if (playerSpecs.size() != requiredPlayerCount) {
            throw new IllegalArgumentException("선수 수는 정확히 " + requiredPlayerCount + "명이어야 합니다");
        }

        List<DraftPlayer> players = playerSpecs.stream()
            .map(DraftPlayer::new)
            .sorted(Comparator.comparingInt(DraftPlayer::displayOrder))
            .toList();

        return new DraftRoom(id, teamCount, teamSize, draftOrderStrategy, players);
    }

    static DraftRoom restore(DraftRoomState state) {
        List<DraftPlayer> players = state.players().stream()
            .map(player -> new DraftPlayer(
                player.playerId(),
                player.name(),
                player.position(),
                player.displayOrder(),
                player.assigned() ? DraftPlayerStatus.ASSIGNED : DraftPlayerStatus.AVAILABLE
            ))
            .sorted(Comparator.comparingInt(DraftPlayer::displayOrder))
            .toList();

        DraftRoom room = new DraftRoom(
            new DraftRoomId(state.roomCode()),
            state.teamCount(),
            state.teamSize(),
            state.draftOrderStrategy(),
            players
        );
        room.leaders.addAll(state.leaders().stream()
            .map(leader -> new DraftLeader(leader.leaderId(), leader.nickname(), leader.draftPosition()))
            .toList());
        room.members.addAll(state.members().stream()
            .map(member -> new DraftMember(member.leaderId(), member.playerId(), member.assignOrder()))
            .toList());
        room.status = state.status();
        room.currentTurnIndex = state.progress() == null ? null : state.progress().currentTurnIndex();
        return room;
    }

    public DraftRoomId getId() {
        return id;
    }

    void addLeader(String leaderId, String nickname) {
        validateWaiting();
        if (leaders.size() >= teamCount) {
            throw new IllegalStateException("드래프트 팀장이 이미 가득 찼습니다");
        }

        String normalizedNickname = normalizeNickname(nickname);
        boolean taken =
            leaders.stream()
                .map(DraftLeader::nickname)
                .map(this::normalizeNickname)
                .anyMatch(normalizedNickname::equals);
        if (taken) {
            throw new IllegalStateException("팀장 닉네임이 이미 사용 중입니다");
        }

        leaders.add(new DraftLeader(leaderId, nickname));
    }

    void selectDraftPosition(String leaderId, int draftPosition) {
        validateDraftPositionChange(draftPosition);

        DraftLeader caller = requireLeader(leaderId);
        boolean taken =
            leaders.stream()
                .filter(leader -> !leader.id().equals(leaderId))
                .anyMatch(leader -> Integer.valueOf(draftPosition).equals(leader.draftPosition()));
        if (taken) {
            throw new IllegalStateException("드래프트 자리가 이미 사용 중입니다");
        }

        caller.assignDraftPosition(draftPosition);
    }

    void clearDraftPosition(String leaderId) {
        validateDraftModeWaiting();
        requireLeader(leaderId).clearDraftPosition();
    }

    void start() {
        validateWaiting();
        DraftRoomReadiness readiness = readiness();
        if (readiness != DraftRoomReadiness.READY) {
            throw DraftRoomStateInvalidException.roomNotReadyForStart(readiness);
        }

        status = DraftRoomStatus.IN_PROGRESS;
        currentTurnIndex = 0;
    }

    DraftMember pick(String leaderId, int playerId) {
        if (status != DraftRoomStatus.IN_PROGRESS) {
            throw DraftRoomStateInvalidException.roomNotInProgress();
        }

        DraftProgress progress = requireCurrentDraftProgress();
        if (!progress.currentLeaderId().equals(leaderId)) {
            throw new IllegalStateException("현재 턴이 아닙니다");
        }

        DraftPlayer player = requireAvailablePlayer(playerId);
        player.assign();

        DraftMember member = new DraftMember(leaderId, player.id().value(), members.size());
        members.add(member);

        currentTurnIndex += 1;
        if (members.size() == players.size()) {
            status = DraftRoomStatus.COMPLETED;
        }

        return member;
    }

    DraftRoomState snapshot() {
        return new DraftRoomState(
            id.value(),
            status,
            readiness(),
            teamCount,
            teamSize,
            draftOrderStrategy,
            leaders.stream()
                .map(leader -> new DraftRoomState.Leader(leader.id(), leader.nickname(), leader.draftPosition()))
                .toList(),
            players.stream()
                .map(player -> new DraftRoomState.Player(
                    player.id().value(),
                    player.name(),
                    player.position(),
                    player.displayOrder(),
                    player.status() == DraftPlayerStatus.ASSIGNED
                ))
                .toList(),
            members.stream()
                .map(member -> new DraftRoomState.Member(member.leaderId(), member.playerId(), member.assignOrder()))
                .toList(),
            new DraftRoomState.OrderPreview(draftOrderPreview()),
            status == DraftRoomStatus.IN_PROGRESS ? currentDraftProgressAsState() : null
        );
    }

    private List<DraftRoomState.OrderSlot> draftOrderPreview() {
        Map<Integer, DraftLeader> leadersByDraftPosition =
            leaders.stream()
                .filter(leader -> leader.draftPosition() != null)
                .collect(java.util.stream.Collectors.toMap(DraftLeader::draftPosition, leader -> leader));

        return IntStream.rangeClosed(1, teamCount)
            .mapToObj(draftPosition -> {
                DraftLeader leader = leadersByDraftPosition.get(draftPosition);
                if (leader == null) {
                    return DraftRoomState.OrderSlot.empty(draftPosition);
                }
                return DraftRoomState.OrderSlot.from(draftPosition, leader);
            })
            .toList();
    }

    private DraftRoomState.Progress currentDraftProgressAsState() {
        DraftProgress progress = requireCurrentDraftProgress();
        return new DraftRoomState.Progress(
            progress.currentTurnIndex(),
            progress.currentRound(),
            progress.currentLeaderId(),
            progress.currentRoundLeaderIds()
        );
    }

    private DraftRoomReadiness readiness() {
        if (status == DraftRoomStatus.IN_PROGRESS) {
            return DraftRoomReadiness.IN_PROGRESS;
        }
        if (status == DraftRoomStatus.COMPLETED) {
            return DraftRoomReadiness.COMPLETED;
        }
        if (leaders.size() < teamCount) {
            return DraftRoomReadiness.WAITING_FOR_LEADERS;
        }
        if (!hasConfirmedDraftPositions()) {
            return DraftRoomReadiness.WAITING_FOR_DRAFT_POSITIONS;
        }
        return DraftRoomReadiness.READY;
    }

    private void validateWaiting() {
        if (status != DraftRoomStatus.WAITING) {
            throw DraftRoomStateInvalidException.roomNotWaiting();
        }
    }

    private void validateDraftModeWaiting() {
        validateWaiting();
    }

    private void validateDraftPositionChange(int draftPosition) {
        validateDraftModeWaiting();
        if (draftPosition < 1 || draftPosition > teamCount) {
            throw new IllegalArgumentException("드래프트 자리는 1 이상 " + teamCount + " 이하여야 합니다");
        }
    }

    private boolean hasConfirmedDraftPositions() {
        return leaders.stream().allMatch(leader -> leader.draftPosition() != null)
            && leaders.stream().map(DraftLeader::draftPosition).distinct().count() == teamCount;
    }

    private DraftLeader requireLeader(String leaderId) {
        return leaders.stream()
            .filter(leader -> leader.id().equals(leaderId))
            .findFirst()
            .orElseThrow(() -> DraftRoomStateInvalidException.leaderMissing(leaderId));
    }

    private DraftPlayer requireAvailablePlayer(int playerId) {
        return players.stream()
            .filter(player -> player.id().value() == playerId)
            .filter(player -> player.status() == DraftPlayerStatus.AVAILABLE)
            .findFirst()
            .orElseThrow(() -> DraftRoomStateInvalidException.playerMissing(playerId));
    }

    private DraftProgress requireCurrentDraftProgress() {
        if (status != DraftRoomStatus.IN_PROGRESS || currentTurnIndex == null) {
            throw DraftRoomStateInvalidException.currentTurnMissing();
        }

        List<String> leaderIds = leadersInDraftOrder();
        try {
            return DraftProgress.from(leaderIds, draftOrderStrategy, currentTurnIndex);
        } catch (IllegalArgumentException ex) {
            throw DraftRoomStateInvalidException.draftLeaderOrderEmpty();
        }
    }

    private List<String> leadersInDraftOrder() {
        if (leaders.isEmpty()) {
            throw DraftRoomStateInvalidException.draftLeaderOrderEmpty();
        }

        DraftLeader leaderWithoutDraftPosition =
            leaders.stream().filter(leader -> leader.draftPosition() == null).findFirst().orElse(null);
        if (leaderWithoutDraftPosition != null) {
            throw DraftRoomStateInvalidException.draftPositionMissing(leaderWithoutDraftPosition.id());
        }

        return leaders.stream()
            .sorted(Comparator.comparingInt(DraftLeader::draftPosition))
            .map(DraftLeader::id)
            .toList();
    }

    private String normalizeNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("팀장 닉네임은 비어 있을 수 없습니다");
        }
        return nickname.trim().toLowerCase(Locale.ROOT);
    }
}
