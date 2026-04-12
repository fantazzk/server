package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.CoreException;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Locale;
import java.util.UUID;
import lombok.Getter;
import org.jmolecules.ddd.types.AggregateRoot;

@Getter
@Access(AccessType.FIELD)
@Table(name = "rooms")
class Room implements AggregateRoot<Room, RoomId> {
    private final RoomId id;
    private final String code;
    @Version
    private long version;
    @Column(nullable = false, updatable = false)
    private final Instant createdAt;
    @Column(name = "host_id")
    @Convert(converter = TeamLeaderId.JpaConverter.class)
    private final TeamLeaderId hostLeaderId;
    @Enumerated(EnumType.STRING)
    private RoomStatus status;
    @Enumerated(EnumType.STRING)
    private final RoomMode mode;
    private final int teamCount;
    private final int teamSize;
    private final Integer budget;
    @Column(name = "pick_ban_time")
    private final int pickBanTime;
    @Enumerated(EnumType.STRING)
    private final RoomTemplateSpec.DraftOrderStrategy draftOrderStrategy;
    private Integer currentTurnIndex;
    private Integer currentAuctionRound;
    @Column(name = "current_auction_round_ends_at")
    private Instant currentAuctionRoundEndsAt;
    @ElementCollection
    @CollectionTable(name = "room_player", joinColumns = @JoinColumn(name = "players_room_id"))
    private final List<RoomPlayer> players;
    @ElementCollection
    @CollectionTable(name = "room_team_leader", joinColumns = @JoinColumn(name = "leaders_room_id"))
    private final List<RoomTeamLeader> leaders;
    @ElementCollection
    @CollectionTable(name = "room_team_member", joinColumns = @JoinColumn(name = "members_room_id"))
    private final List<RoomTeamMember> members;
    @ElementCollection
    @CollectionTable(name = "room_bid", joinColumns = @JoinColumn(name = "bids_room_id"))
    private final List<RoomBid> bids;

    Room(
        String code,
        Instant createdAt,
        TeamLeaderId hostLeaderId,
        RoomMode mode,
        int teamCount,
        int teamSize,
        Integer budget,
        int pickBanTime,
        RoomTemplateSpec.DraftOrderStrategy draftOrderStrategy
    ) {
        this.id = new RoomId(UUID.randomUUID());
        this.code = code;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.hostLeaderId = Objects.requireNonNull(hostLeaderId, "hostLeaderId must not be null");
        this.status = RoomStatus.WAITING;
        this.mode = mode;
        this.teamCount = teamCount;
        this.teamSize = teamSize;
        this.budget = budget;
        this.pickBanTime = pickBanTime;
        this.draftOrderStrategy = draftOrderStrategy;
        this.currentTurnIndex = null;
        this.currentAuctionRound = null;
        this.currentAuctionRoundEndsAt = null;
        this.players = new ArrayList<>();
        this.leaders = new ArrayList<>();
        this.members = new ArrayList<>();
        this.bids = new ArrayList<>();
    }

    public static Room createFromTemplate(
        String code,
        TeamLeaderId hostLeaderId,
        String hostNickname,
        String hostActionToken,
        RoomTemplateSpec spec,
        Instant createdAt
    ) {
        Room room =
            new Room(
                code,
                createdAt,
                hostLeaderId,
                spec.mode() == RoomTemplateSpec.Mode.AUCTION ? RoomMode.AUCTION : RoomMode.DRAFT,
                spec.teamCount(),
                spec.teamSize(),
                spec.budget(),
                spec.pickBanTime(),
                spec.draftOrderStrategy()
            );

        spec.players().stream()
            .sorted(Comparator.comparingInt(RoomTemplateSpec.Player::displayOrder))
            .map(player -> new RoomPlayer(player.id(), player.name(), player.position(), player.displayOrder()))
            .forEach(room.players::add);

        room.leaders.add(new RoomTeamLeader(hostLeaderId, hostNickname.trim(), hostActionToken, spec.budget()));
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

    public void join(TeamLeaderId teamLeaderId, String nickname, String actionToken) {
        if (status != RoomStatus.WAITING) {
            throw CoreException.of(RoomErrorType.ROOM_JOIN_REQUIRES_WAITING);
        }
        if (leaders.size() >= teamCount) {
            throw CoreException.of(RoomErrorType.ROOM_FULL);
        }

        String normalizedNickname = normalizeNickname(nickname);
        boolean taken =
            leaders.stream()
                .map(RoomTeamLeader::getNickname)
                .map(this::normalizeNickname)
                .anyMatch(normalizedNickname::equals);
        if (taken) {
            throw CoreException.of(RoomErrorType.ROOM_NICKNAME_ALREADY_TAKEN);
        }

        leaders.add(new RoomTeamLeader(teamLeaderId, nickname.trim(), actionToken, budget));
    }

    private String normalizeNickname(String nickname) {
        return nickname.trim().toLowerCase(Locale.ROOT);
    }

    void selectDraftPosition(TeamLeaderId callerLeaderId, int draftPosition) {
        validateDraftPositionChange(draftPosition);

        RoomTeamLeader caller = getLeader(callerLeaderId);
        boolean taken =
            leaders.stream()
                .filter(leader -> !leader.getId().equals(callerLeaderId))
                .anyMatch(leader -> Integer.valueOf(draftPosition).equals(leader.getDraftPosition()));
        if (taken) {
            throw CoreException.of(RoomErrorType.ROOM_DRAFT_POSITION_TAKEN);
        }

        caller.assignDraftPosition(draftPosition);
    }

    void clearDraftPosition(TeamLeaderId callerLeaderId) {
        validateDraftModeWaiting();
        getLeader(callerLeaderId).clearDraftPosition();
    }

    void start(TeamLeaderId callerLeaderId) {
        start(callerLeaderId, Instant.now());
    }

    void start(TeamLeaderId callerLeaderId, Instant now) {
        if (!hostLeaderId.equals(callerLeaderId)) {
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
            currentAuctionRoundEndsAt = now.plusSeconds(pickBanTime);
        } else {
            currentTurnIndex = 0;
            currentAuctionRound = null;
            currentAuctionRoundEndsAt = null;
        }
    }

    RoomBid placeBid(TeamLeaderId teamLeaderId, int amount) {
        return placeBid(teamLeaderId, amount, Instant.now());
    }

    RoomBid placeBid(TeamLeaderId teamLeaderId, int amount, Instant now) {
        if (status != RoomStatus.IN_PROGRESS) {
            throw CoreException.of(RoomErrorType.ROOM_PLAY_REQUIRES_IN_PROGRESS);
        }
        if (mode != RoomMode.AUCTION) {
            throw CoreException.of(RoomErrorType.ROOM_BID_REQUIRES_AUCTION_MODE);
        }
        if (currentAuctionRound == null) {
            throw RoomStateInvalidException.auctionRoundMissing();
        }
        if (currentAuctionRoundEndsAt != null && !now.isBefore(currentAuctionRoundEndsAt)) {
            throw CoreException.of(RoomErrorType.ROOM_BID_REQUIRES_OPEN_ROUND);
        }
        if (amount <= 0) {
            throw CoreException.of(RoomErrorType.ROOM_BID_AMOUNT_NOT_POSITIVE);
        }

        RoomTeamLeader leader =
            leaders.stream()
                .filter(it -> it.getId().equals(teamLeaderId))
                .findFirst()
                .orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_BIDDER_NOT_FOUND));

        if (leader.getRemainingBudget() != null && leader.getRemainingBudget() < amount) {
            throw CoreException.of(RoomErrorType.ROOM_BID_BUDGET_EXCEEDED);
        }

        bids.stream()
            .filter(it -> it.round() == currentAuctionRound)
            .mapToInt(RoomBid::amount)
            .max()
            .ifPresent(highest -> {
                if (amount <= highest) {
                    throw CoreException.of(RoomErrorType.ROOM_BID_TOO_LOW);
                }
            });

        BidSequence nextSequence =
            new BidSequence(
                bids.stream()
                    .filter(it -> it.round() == currentAuctionRound)
                    .map(RoomBid::sequence)
                    .mapToInt(BidSequence::value)
                    .max()
                    .orElse(0) + 1
            );
        RoomBid bid = new RoomBid(currentAuctionRound, nextSequence, teamLeaderId, amount);
        bids.add(bid);
        repairMissingAuctionDeadline(now);
        return bid;
    }

    boolean repairMissingAuctionDeadline(Instant now) {
        if (status != RoomStatus.IN_PROGRESS || mode != RoomMode.AUCTION || currentAuctionRound == null) {
            return false;
        }
        if (currentAuctionRoundEndsAt != null) {
            return false;
        }
        currentAuctionRoundEndsAt = now.plusSeconds(pickBanTime);
        return true;
    }

    public AuctionSettlement settleAuction() {
        return settleAuction(Instant.now());
    }

    public AuctionSettlement settleAuction(Instant now) {
        if (status != RoomStatus.IN_PROGRESS) {
            throw CoreException.of(RoomErrorType.ROOM_PLAY_REQUIRES_IN_PROGRESS);
        }
        if (mode != RoomMode.AUCTION) {
            throw CoreException.of(RoomErrorType.ROOM_BID_REQUIRES_AUCTION_MODE);
        }
        if (currentAuctionRound == null) {
            throw RoomStateInvalidException.auctionRoundMissing();
        }
        if (currentAuctionRoundEndsAt == null) {
            currentAuctionRoundEndsAt = now;
        }
        if (now.isBefore(currentAuctionRoundEndsAt)) {
            throw CoreException.of(RoomErrorType.ROOM_AUCTION_ROUND_NOT_ENDED);
        }

        RoomPlayer target =
            players.stream()
                .filter(it -> it.getStatus() == PlayerStatus.AVAILABLE)
                .min(Comparator.comparingInt(RoomPlayer::getDisplayOrder))
                .orElseThrow(RoomStateInvalidException::auctionTargetMissing);

        RoomBid winningBid =
            bids.stream()
                .filter(it -> it.round() == currentAuctionRound)
                .max(Comparator.comparingInt(RoomBid::amount))
                .orElse(null);

        if (winningBid == null) {
            int maxOrder = players.stream().mapToInt(RoomPlayer::getDisplayOrder).max().orElse(0);
            target.moveToBack(maxOrder + 1);
            currentAuctionRound += 1;
            currentAuctionRoundEndsAt = now.plusSeconds(pickBanTime);
            return new AuctionSettlement(target.getName(), AuctionOutcome.PASSED);
        }

        RoomTeamLeader winner =
            leaders.stream()
                .filter(it -> it.getId().equals(winningBid.teamLeaderId()))
                .findFirst()
                .orElseThrow(() -> RoomStateInvalidException.auctionWinnerMissing(winningBid.teamLeaderId()));

        target.assign();
        winner.spend(winningBid.amount());
        members.add(new RoomTeamMember(winningBid.teamLeaderId(), target.getName(), members.size()));

        if (members.size() == teamCount * (teamSize - 1)) {
            status = RoomStatus.COMPLETED;
            currentAuctionRoundEndsAt = null;
        } else {
            currentAuctionRound += 1;
            currentAuctionRoundEndsAt = now.plusSeconds(pickBanTime);
        }

        return new AuctionSettlement(target.getName(), AuctionOutcome.SOLD);
    }

    RoomTeamMember pick(TeamLeaderId teamLeaderId, String playerName) {
        if (status != RoomStatus.IN_PROGRESS) {
            throw CoreException.of(RoomErrorType.ROOM_PLAY_REQUIRES_IN_PROGRESS);
        }
        if (mode != RoomMode.DRAFT) {
            throw CoreException.of(RoomErrorType.ROOM_PICK_REQUIRES_DRAFT_MODE);
        }

        DraftProgress progress = requireCurrentDraftProgress();
        if (!progress.currentLeaderId().equals(teamLeaderId.value())) {
            throw CoreException.of(RoomErrorType.ROOM_PICK_OUT_OF_TURN);
        }

        RoomPlayer player =
            players.stream()
                .filter(it -> it.getName().equals(playerName))
                .filter(it -> it.getStatus() == PlayerStatus.AVAILABLE)
                .findFirst()
                .orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_PICK_PLAYER_NOT_AVAILABLE));

        player.assign();
        RoomTeamMember member = new RoomTeamMember(teamLeaderId, player.getName(), members.size());
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

        try {
            return DraftProgress.from(getLeaderIdsInDraftOrder(), draftOrderStrategy, currentTurnIndex);
        } catch (IllegalArgumentException ex) {
            throw RoomStateInvalidException.draftLeaderOrderEmpty();
        }
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
        if (leaders.isEmpty()) {
            throw RoomStateInvalidException.draftLeaderOrderEmpty();
        }
        RoomTeamLeader leaderWithoutDraftPosition =
            leaders.stream().filter(leader -> leader.getDraftPosition() == null).findFirst().orElse(null);
        if (leaderWithoutDraftPosition != null) {
            throw RoomStateInvalidException.draftPositionMissing(leaderWithoutDraftPosition.getId());
        }
        return leaders.stream()
            .sorted(Comparator.comparingInt(RoomTeamLeader::getDraftPosition))
            .toList();
    }

    private DraftProgress requireCurrentDraftProgress() {
        DraftProgress progress = currentDraftProgress();
        if (progress == null) {
            throw RoomStateInvalidException.draftTurnMissing();
        }
        return progress;
    }

    private List<String> getLeaderIdsInDraftOrder() {
        return getLeadersInDraftOrder().stream().map(RoomTeamLeader::getTeamLeaderId).toList();
    }

    private RoomTeamLeader getLeader(TeamLeaderId teamLeaderId) {
        return leaders.stream()
            .filter(leader -> leader.getId().equals(teamLeaderId))
            .findFirst()
            .orElseThrow(() -> RoomStateInvalidException.leaderMissing(teamLeaderId));
    }
}
