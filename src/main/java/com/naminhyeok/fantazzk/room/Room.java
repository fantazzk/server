package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.CoreException;
import jakarta.persistence.EnumType;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import org.jmolecules.ddd.types.AggregateRoot;

@Getter
@Table(name = "rooms")
class Room implements AggregateRoot<Room, RoomId> {
    private final RoomId id;
    private final String code;
    @Column(nullable = false, updatable = false)
    private final Instant createdAt;
    private final String hostId;
    @Enumerated(EnumType.STRING)
    private RoomStatus status;
    @Enumerated(EnumType.STRING)
    private final RoomMode mode;
    private final int teamCount;
    private final int teamSize;
    private final Integer budget;
    @Enumerated(EnumType.STRING)
    private final RoomTemplateSpec.DraftOrderStrategy draftOrderStrategy;
    private Integer currentTurnIndex;
    private Integer currentAuctionRound;
    private final List<RoomPlayer> players;
    private final List<RoomTeamLeader> leaders;
    private final List<RoomTeamMember> members;
    private final List<RoomBid> bids;

    Room(
        String code,
        Instant createdAt,
        String hostId,
        RoomMode mode,
        int teamCount,
        int teamSize,
        Integer budget,
        RoomTemplateSpec.DraftOrderStrategy draftOrderStrategy
    ) {
        this.id = new RoomId(UUID.randomUUID());
        this.code = code;
        this.createdAt = createdAt;
        this.hostId = hostId;
        this.status = RoomStatus.WAITING;
        this.mode = mode;
        this.teamCount = teamCount;
        this.teamSize = teamSize;
        this.budget = budget;
        this.draftOrderStrategy = draftOrderStrategy;
        this.currentTurnIndex = null;
        this.currentAuctionRound = null;
        this.players = new ArrayList<>();
        this.leaders = new ArrayList<>();
        this.members = new ArrayList<>();
        this.bids = new ArrayList<>();
    }

    public static Room createFromTemplate(
        String code,
        String hostId,
        String hostNickname,
        String hostActionToken,
        RoomTemplateSpec spec,
        Instant createdAt
    ) {
        Room room =
            new Room(
                code,
                createdAt,
                hostId,
                spec.mode() == RoomTemplateSpec.Mode.AUCTION ? RoomMode.AUCTION : RoomMode.DRAFT,
                spec.teamCount(),
                spec.teamSize(),
                spec.budget(),
                spec.draftOrderStrategy()
            );

        spec.players().stream()
            .sorted(Comparator.comparingInt(RoomTemplateSpec.Player::displayOrder))
            .map(player -> new RoomPlayer(player.name(), player.displayOrder()))
            .forEach(room.players::add);

        room.leaders.add(new RoomTeamLeader(hostId, hostNickname, hostActionToken, spec.budget()));
        return room;
    }

    public List<RoomPlayer> getPlayers() {
        return players.stream().sorted(Comparator.comparingInt(RoomPlayer::getDisplayOrder)).toList();
    }

    public List<RoomTeamLeader> getLeaders() {
        return List.copyOf(leaders);
    }

    public List<RoomTeamMember> getMembers() {
        return List.copyOf(members);
    }

    public RoomStartReadiness getStartReadiness() {
        if (status != RoomStatus.WAITING) {
            return RoomStartReadiness.NOT_WAITING;
        }
        if (leaders.size() != teamCount) {
            return RoomStartReadiness.WAITING_FOR_LEADERS;
        }
        if (mode == RoomMode.DRAFT && !hasConfirmedDraftPositions()) {
            return RoomStartReadiness.WAITING_FOR_DRAFT_POSITIONS;
        }
        return RoomStartReadiness.STARTABLE;
    }

    boolean isJoinable() {
        return status == RoomStatus.WAITING && leaders.size() < teamCount;
    }

    public void join(String teamLeaderId, String nickname, String actionToken) {
        if (status != RoomStatus.WAITING) {
            throw CoreException.of(RoomErrorType.ROOM_JOIN_REQUIRES_WAITING);
        }
        if (leaders.size() >= teamCount) {
            throw CoreException.of(RoomErrorType.ROOM_FULL);
        }
        leaders.add(new RoomTeamLeader(teamLeaderId, nickname, actionToken, budget));
    }

    public void selectDraftPosition(String callerLeaderId, int draftPosition) {
        validateDraftPositionChange(draftPosition);

        RoomTeamLeader caller = getLeader(callerLeaderId);
        boolean taken =
            leaders.stream()
                .filter(leader -> !leader.getTeamLeaderId().equals(callerLeaderId))
                .anyMatch(leader -> Integer.valueOf(draftPosition).equals(leader.getDraftPosition()));
        if (taken) {
            throw CoreException.of(RoomErrorType.ROOM_DRAFT_POSITION_TAKEN);
        }

        caller.assignDraftPosition(draftPosition);
    }

    public void clearDraftPosition(String callerLeaderId) {
        validateDraftModeWaiting();
        getLeader(callerLeaderId).clearDraftPosition();
    }

    public void start(String callerLeaderId) {
        if (!hostId.equals(callerLeaderId)) {
            throw CoreException.of(RoomErrorType.ROOM_START_FORBIDDEN);
        }
        RoomStartReadiness readiness = getStartReadiness();
        if (readiness == RoomStartReadiness.NOT_WAITING) {
            throw CoreException.of(RoomErrorType.ROOM_START_REQUIRES_WAITING);
        }
        if (readiness == RoomStartReadiness.WAITING_FOR_LEADERS) {
            throw CoreException.of(RoomErrorType.ROOM_LEADERS_NOT_FULL);
        }
        if (readiness == RoomStartReadiness.WAITING_FOR_DRAFT_POSITIONS) {
            throw CoreException.of(RoomErrorType.ROOM_DRAFT_POSITIONS_NOT_FULL);
        }

        status = RoomStatus.IN_PROGRESS;

        if (mode == RoomMode.AUCTION) {
            currentAuctionRound = 1;
            currentTurnIndex = null;
        } else {
            currentTurnIndex = 0;
            currentAuctionRound = null;
        }
    }

    public RoomBid placeBid(String teamLeaderId, int amount) {
        if (status != RoomStatus.IN_PROGRESS) {
            throw CoreException.of(RoomErrorType.ROOM_PLAY_REQUIRES_IN_PROGRESS);
        }
        if (mode != RoomMode.AUCTION) {
            throw CoreException.of(RoomErrorType.ROOM_BID_REQUIRES_AUCTION_MODE);
        }
        if (currentAuctionRound == null) {
            throw new IllegalStateException("현재 경매 라운드가 없습니다");
        }
        if (amount <= 0) {
            throw CoreException.of(RoomErrorType.ROOM_BID_AMOUNT_NOT_POSITIVE);
        }

        RoomTeamLeader leader =
            leaders.stream()
                .filter(it -> it.getTeamLeaderId().equals(teamLeaderId))
                .findFirst()
                .orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_BIDDER_NOT_FOUND));

        if (leader.getRemainingBudget() != null && leader.getRemainingBudget() < amount) {
            throw CoreException.of(RoomErrorType.ROOM_BID_BUDGET_EXCEEDED);
        }

        bids.stream()
            .filter(it -> it.getRound() == currentAuctionRound)
            .mapToInt(RoomBid::getAmount)
            .max()
            .ifPresent(highest -> {
                if (amount <= highest) {
                    throw CoreException.of(RoomErrorType.ROOM_BID_TOO_LOW);
                }
            });

        RoomBid bid = new RoomBid(currentAuctionRound, teamLeaderId, amount);
        bids.add(bid);
        return bid;
    }

    public AuctionSettlement settleAuction() {
        if (status != RoomStatus.IN_PROGRESS) {
            throw CoreException.of(RoomErrorType.ROOM_PLAY_REQUIRES_IN_PROGRESS);
        }
        if (mode != RoomMode.AUCTION) {
            throw CoreException.of(RoomErrorType.ROOM_BID_REQUIRES_AUCTION_MODE);
        }
        if (currentAuctionRound == null) {
            throw new IllegalStateException("현재 경매 라운드가 없습니다");
        }

        RoomPlayer target =
            players.stream()
                .filter(it -> it.getStatus() == PlayerStatus.AVAILABLE)
                .min(Comparator.comparingInt(RoomPlayer::getDisplayOrder))
                .orElseThrow(() -> new IllegalStateException("경매할 선수를 찾을 수 없습니다"));

        RoomBid winningBid =
            bids.stream()
                .filter(it -> it.getRound() == currentAuctionRound)
                .max(Comparator.comparingInt(RoomBid::getAmount))
                .orElse(null);

        currentAuctionRound += 1;

        if (winningBid == null) {
            int maxOrder = players.stream().mapToInt(RoomPlayer::getDisplayOrder).max().orElse(0);
            target.moveToBack(maxOrder + 1);
            return new AuctionSettlement(target.getName(), AuctionOutcome.PASSED);
        }

        RoomTeamLeader winner =
            leaders.stream()
                .filter(it -> it.getTeamLeaderId().equals(winningBid.getTeamLeaderId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("낙찰한 팀장을 찾을 수 없습니다"));

        target.assign();
        winner.spend(winningBid.getAmount());
        members.add(new RoomTeamMember(winningBid.getTeamLeaderId(), target.getName(), members.size()));

        if (members.size() == teamCount * (teamSize - 1)) {
            status = RoomStatus.COMPLETED;
        }

        return new AuctionSettlement(target.getName(), AuctionOutcome.SOLD);
    }

    public RoomTeamMember pick(String teamLeaderId, String playerName) {
        if (status != RoomStatus.IN_PROGRESS) {
            throw CoreException.of(RoomErrorType.ROOM_PLAY_REQUIRES_IN_PROGRESS);
        }
        if (mode != RoomMode.DRAFT) {
            throw CoreException.of(RoomErrorType.ROOM_PICK_REQUIRES_DRAFT_MODE);
        }
        DraftProgress progress = requireCurrentDraftProgress();
        if (!progress.currentLeaderId().equals(teamLeaderId)) {
            throw CoreException.of(RoomErrorType.ROOM_PICK_OUT_OF_TURN);
        }

        RoomPlayer player =
            players.stream()
                .filter(it -> it.getName().equals(playerName))
                .filter(it -> it.getStatus() == PlayerStatus.AVAILABLE)
                .findFirst()
                .orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_PICK_PLAYER_NOT_AVAILABLE));

        player.assign();
        RoomTeamMember member = new RoomTeamMember(teamLeaderId, playerName, members.size());
        members.add(member);

        currentTurnIndex += 1;
        if (members.size() == teamCount * (teamSize - 1)) {
            status = RoomStatus.COMPLETED;
        }

        return member;
    }

    DraftProgress currentDraftProgress() {
        if (mode != RoomMode.DRAFT || status != RoomStatus.IN_PROGRESS || currentTurnIndex == null) {
            return null;
        }

        return DraftProgress.from(getLeaderIdsInDraftOrder(), draftOrderStrategy, currentTurnIndex);
    }

    private void validateDraftPositionChange(int draftPosition) {
        validateDraftModeWaiting();
        if (draftPosition < 1 || draftPosition > teamCount) {
            throw CoreException.of(RoomErrorType.ROOM_DRAFT_POSITION_OUT_OF_RANGE);
        }
    }

    private void validateDraftModeWaiting() {
        if (mode != RoomMode.DRAFT) {
            throw CoreException.of(RoomErrorType.ROOM_DRAFT_POSITION_REQUIRES_DRAFT_MODE);
        }
        if (status != RoomStatus.WAITING) {
            throw CoreException.of(RoomErrorType.ROOM_DRAFT_POSITION_REQUIRES_WAITING);
        }
    }

    private boolean hasConfirmedDraftPositions() {
        return leaders.stream().allMatch(leader -> leader.getDraftPosition() != null)
            && leaders.stream().map(RoomTeamLeader::getDraftPosition).distinct().count() == teamCount;
    }

    private List<RoomTeamLeader> getLeadersInDraftOrder() {
        return leaders.stream()
            .sorted(Comparator.comparingInt(RoomTeamLeader::getDraftPosition))
            .toList();
    }

    private DraftProgress requireCurrentDraftProgress() {
        DraftProgress progress = currentDraftProgress();
        if (progress == null) {
            throw new IllegalStateException("현재 드래프트 턴이 없습니다");
        }
        return progress;
    }

    private List<String> getLeaderIdsInDraftOrder() {
        return getLeadersInDraftOrder().stream().map(RoomTeamLeader::getTeamLeaderId).toList();
    }

    private RoomTeamLeader getLeader(String teamLeaderId) {
        return leaders.stream()
            .filter(leader -> leader.getTeamLeaderId().equals(teamLeaderId))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("팀장을 찾을 수 없습니다"));
    }
}
