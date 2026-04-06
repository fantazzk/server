package com.naminhyeok.fantazzk.room.domain;

import com.naminhyeok.fantazzk.room.RoomCode;
import com.naminhyeok.fantazzk.room.RoomId;
import com.naminhyeok.fantazzk.room.application.RoomTemplateSpec;
import com.naminhyeok.fantazzk.room.exception.RoomException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.jmolecules.ddd.types.AggregateRoot;
import org.springframework.lang.Nullable;

@Entity
@Table(name = "room")
public class Room implements AggregateRoot<Room, RoomId> {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID persistentId;

    @Column(name = "code", nullable = false, unique = true, length = 6)
    private RoomCode roomCode;

    @Column(name = "host_id", nullable = false, length = 36)
    private String hostId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RoomStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false, length = 20)
    private TeamBuildingMode mode;

    @Column(name = "team_count", nullable = false)
    private int teamCount;

    @Column(name = "team_size", nullable = false)
    private int teamSize;

    @Column(name = "budget")
    private Integer budget;

    @Enumerated(EnumType.STRING)
    @Column(name = "draft_order_strategy")
    private DraftOrderStrategy draftOrderStrategy;

    @Column(name = "current_turn_index")
    private Integer currentTurnIndex;

    @Column(name = "current_auction_round")
    private Integer currentAuctionRound;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<RoomPlayer> players = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<RoomTeamLeader> leaders = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("assignOrder ASC")
    private List<RoomTeamMember> members = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("round ASC, createdAt ASC")
    private List<RoomBid> bidHistory = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Room() {
        this.persistentId = UUID.randomUUID();
        this.roomCode = RoomCode.of("ROOM00");
        this.hostId = "system";
        this.status = RoomStatus.WAITING;
        this.mode = TeamBuildingMode.AUCTION;
        this.teamCount = 1;
        this.teamSize = 2;
        this.budget = 1;
        this.draftOrderStrategy = null;
        this.currentTurnIndex = null;
        this.currentAuctionRound = null;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    private Room(
            RoomId roomId,
            RoomCode roomCode,
            String hostId,
            RoomStatus status,
            TeamBuildingMode mode,
            int teamCount,
            int teamSize,
            Integer budget,
            DraftOrderStrategy draftOrderStrategy,
            Integer currentTurnIndex,
            Integer currentAuctionRound,
            List<RoomPlayer> players,
            List<RoomTeamLeader> leaders,
            List<RoomTeamMember> members,
            List<RoomBid> bidHistory,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.persistentId = roomId == null ? UUID.randomUUID() : roomId.getValue();
        this.roomCode = Objects.requireNonNull(roomCode, "roomCode");
        this.hostId = requireText(hostId, "호스트 식별자는 비어 있을 수 없습니다");
        this.status = Objects.requireNonNull(status, "status");
        this.mode = Objects.requireNonNull(mode, "mode");
        this.teamCount = teamCount;
        this.teamSize = teamSize;
        this.budget = budget;
        this.draftOrderStrategy = draftOrderStrategy;
        this.currentTurnIndex = currentTurnIndex;
        this.currentAuctionRound = currentAuctionRound;
        this.players = copyPlayers(getRoomId(), players);
        this.leaders = copyLeaders(getRoomId(), leaders);
        this.members = copyMembers(getRoomId(), members);
        this.bidHistory = copyBids(getRoomId(), bidHistory);
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
        attachGraph();
        validateState();
    }

    public static Room createAuction(String code, String hostId, int teamCount, int teamSize, int budget) {
        Instant now = Instant.now();
        RoomId roomId = RoomId.random();
        return new Room(
                roomId,
                RoomCode.of(code),
                hostId,
                RoomStatus.WAITING,
                TeamBuildingMode.AUCTION,
                teamCount,
                teamSize,
                budget,
                null,
                null,
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                now,
                now);
    }

    public static Room createDraft(String code, String hostId, int teamCount, int teamSize, DraftOrderStrategy draftOrderStrategy) {
        Instant now = Instant.now();
        RoomId roomId = RoomId.random();
        return new Room(
                roomId,
                RoomCode.of(code),
                hostId,
                RoomStatus.WAITING,
                TeamBuildingMode.DRAFT,
                teamCount,
                teamSize,
                null,
                draftOrderStrategy,
                null,
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                now,
                now);
    }

    static Room reference(RoomId roomId) {
        return new Room(
                roomId,
                RoomCode.of("ROOM00"),
                "system",
                RoomStatus.WAITING,
                TeamBuildingMode.AUCTION,
                1,
                2,
                1,
                null,
                null,
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                Instant.now(),
                Instant.now());
    }

    public static Room restore(
            RoomId roomId,
            String code,
            String hostId,
            RoomStatus status,
            TeamBuildingMode mode,
            int teamCount,
            int teamSize,
            Integer budget,
            DraftOrderStrategy draftOrderStrategy,
            Integer currentTurnIndex,
            Integer currentAuctionRound,
            List<RoomPlayer> players,
            List<RoomTeamLeader> leaders,
            List<RoomTeamMember> members,
            List<RoomBid> bidHistory,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new Room(
                roomId,
                RoomCode.of(code),
                hostId,
                status,
                mode,
                teamCount,
                teamSize,
                budget,
                draftOrderStrategy,
                currentTurnIndex,
                currentAuctionRound,
                players,
                leaders,
                members,
                bidHistory,
                createdAt,
                updatedAt);
    }

    public static Room createFromTemplate(String code, String hostId, String hostNickname, RoomTemplateSpec spec) {
        Room room = switch (spec.getMode()) {
            case AUCTION -> createAuction(
                    code,
                    hostId,
                    spec.getTeamCount(),
                    spec.getTeamSize(),
                    requireNonNullValue(spec.getBudget(), "경매 템플릿에는 예산이 필요합니다"));
            case DRAFT -> createDraft(
                    code,
                    hostId,
                    spec.getTeamCount(),
                    spec.getTeamSize(),
                    DraftOrderStrategy.valueOf(
                            requireNonNullValue(spec.getDraftOrderStrategy(), "드래프트 템플릿에는 순서 전략이 필요합니다")
                                    .name()));
        };

        room.players.clear();
        spec.getPlayers().stream()
                .sorted(Comparator.comparingInt(RoomTemplateSpec.Player::getDisplayOrder))
                .map(player -> RoomPlayer.create(room.getRoomId(), player.getName(), player.getDisplayOrder()))
                .forEach(room::addPlayer);
        room.addLeader(room.createHostLeader(hostNickname).copy());
        return room;
    }

    public RoomTeamLeader createHostLeader(String nickname) {
        return createTeamLeader(hostId, nickname);
    }

    public Room requireJoinable(int currentLeaderCount) {
        checkState(isWaiting(), "대기 중인 방에서만 참가할 수 있습니다");
        checkState(currentLeaderCount < teamCount, "방이 가득 찼습니다");
        return this;
    }

    public RoomTeamLeader join(String teamLeaderId, String nickname, int currentLeaderCount) {
        requireJoinable(currentLeaderCount);
        return createTeamLeader(teamLeaderId, nickname);
    }

    public Room join(String nickname) {
        return join(nickname, UUID.randomUUID().toString());
    }

    public Room join(String nickname, String teamLeaderId) {
        RoomTeamLeader leader = join(teamLeaderId, nickname, leaders.size());
        addLeader(leader.copy());
        return this;
    }

    public Room start(int leaderCount) {
        checkState(isWaiting(), "대기 중인 방에서만 시작할 수 있습니다");
        checkState(leaderCount == teamCount, "모든 팀장 자리가 채워져야 시작할 수 있습니다");
        if (mode == TeamBuildingMode.AUCTION) {
            status = RoomStatus.IN_PROGRESS;
            currentAuctionRound = 1;
            currentTurnIndex = null;
        } else {
            status = RoomStatus.IN_PROGRESS;
            currentTurnIndex = 0;
            currentAuctionRound = null;
        }
        return this;
    }

    public Room start() {
        return start(leaders.size());
    }

    public int requireCurrentAuctionRound() {
        return requireNonNullValue(currentAuctionRound, "현재 경매 라운드가 없습니다");
    }

    public int requireCurrentTurnIndex() {
        return requireNonNullValue(currentTurnIndex, "현재 드래프트 턴이 없습니다");
    }

    public Room advanceAuction(int nextRound, boolean completed) {
        checkState(isAuction(), "경매 모드가 아닙니다");
        checkState(isInProgress(), "진행 중인 방에서만 가능합니다");
        requireNextAuctionRound(nextRound);
        currentAuctionRound = nextRound;
        currentTurnIndex = null;
        if (completed) {
            status = RoomStatus.COMPLETED;
        }
        return this;
    }

    public Room moveAuctionTargetToNextRound(int nextRound) {
        checkState(isAuction(), "경매 모드가 아닙니다");
        checkState(isInProgress(), "진행 중인 방에서만 가능합니다");
        requireNextAuctionRound(nextRound);
        currentAuctionRound = nextRound;
        currentTurnIndex = null;
        return this;
    }

    public Room advanceDraftTurn(int nextTurnIndex, boolean completed) {
        checkState(isDraft(), "드래프트 모드가 아닙니다");
        checkState(isInProgress(), "진행 중인 방에서만 가능합니다");
        requireNextDraftTurn(nextTurnIndex);
        currentTurnIndex = nextTurnIndex;
        currentAuctionRound = null;
        if (completed) {
            status = RoomStatus.COMPLETED;
        }
        return this;
    }

    public Room placeBid(String teamLeaderId, int amount) {
        checkState(isInProgress(), "진행 중인 방에서만 가능합니다");
        checkState(isAuction(), "경매 모드가 아닙니다");

        int currentRound = requireCurrentAuctionRound();
        RoomTeamLeader leader = leaders.stream()
                .filter(candidate -> candidate.getTeamLeaderId().equals(teamLeaderId))
                .findFirst()
                .orElseThrow(RoomException.TeamLeaderNotFoundException::new);
        leader.requireCanBid(amount);

        RoomBid highest = getBids().stream().max(Comparator.comparingInt(RoomBid::getAmount)).orElse(null);
        new AuctionRound(currentRound, highest).requireHigherBid(amount);
        addBid(RoomBid.create(getRoomId(), currentRound, teamLeaderId, amount));
        return this;
    }

    public Room settleAuction() {
        checkState(isInProgress(), "진행 중인 방에서만 가능합니다");
        checkState(isAuction(), "경매 모드가 아닙니다");

        int currentRound = requireCurrentAuctionRound();
        RoomPlayer target = players.stream()
                .filter(player -> player.getStatus() == PlayerStatus.AVAILABLE)
                .min(Comparator.comparingInt(RoomPlayer::getDisplayOrder))
                .orElseThrow(() -> new IllegalArgumentException("경매할 선수가 없습니다"));
        RoomBid highest = getBids().stream().max(Comparator.comparingInt(RoomBid::getAmount)).orElse(null);
        int assignedCountAfterSettlement = members.size() + 1;
        int totalRequired = teamCount * picksPerTeam();
        AuctionRoundSettlement settlement = new AuctionRound(currentRound, highest)
                .settle(target.getName(), assignedCountAfterSettlement, totalRequired);

        if (settlement.getOutcome() == AuctionOutcome.SOLD) {
            return settleSold(target, settlement);
        }
        return settlePassed(target, settlement);
    }

    public Room pick(String teamLeaderId, String playerName) {
        checkState(isInProgress(), "진행 중인 방에서만 가능합니다");
        checkState(isDraft(), "드래프트 모드가 아닙니다");

        int turnIndex = requireCurrentTurnIndex();
        DraftOrderStrategy strategy = requireNonNullValue(draftOrderStrategy, "드래프트 모드에는 순서 전략이 필요합니다");
        DraftBoard board = new DraftBoard(leaders.stream().map(RoomTeamLeader::getTeamLeaderId).toList(), strategy, picksPerTeam());
        board.requireTurnOwner(turnIndex, teamLeaderId);
        leaders.stream()
                .filter(candidate -> candidate.getTeamLeaderId().equals(teamLeaderId))
                .findFirst()
                .orElseThrow(RoomException.TeamLeaderNotFoundException::new);
        RoomPlayer target = players.stream()
                .filter(player -> player.getName().equals(playerName) && player.getStatus() == PlayerStatus.AVAILABLE)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("선수 '" + playerName + "'은(는) 선택할 수 없습니다"));

        int assignedCount = members.size();
        DraftPickSettlement settlement = board.settlePick(turnIndex, assignedCount + 1);
        advanceDraftTurn(settlement.getNextTurnIndex(), settlement.isCompleted());
        target.assign();
        addMember(RoomTeamMember.create(getRoomId(), teamLeaderId, playerName, assignedCount));
        return this;
    }

    @Override
    public RoomId getId() {
        return getRoomId();
    }

    public RoomId getRoomId() {
        return RoomId.from(Objects.requireNonNull(persistentId, "roomId is not assigned"));
    }

    public RoomCode getRoomCode() {
        return roomCode;
    }

    public String getCode() {
        return roomCode.getValue();
    }

    public String getHostId() {
        return hostId;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public TeamBuildingMode getMode() {
        return mode;
    }

    public int getTeamCount() {
        return teamCount;
    }

    public int getTeamSize() {
        return teamSize;
    }

    @Nullable
    public Integer getBudget() {
        return budget;
    }

    @Nullable
    public DraftOrderStrategy getDraftOrderStrategy() {
        return draftOrderStrategy;
    }

    @Nullable
    public Integer getCurrentTurnIndex() {
        return currentTurnIndex;
    }

    @Nullable
    public Integer getCurrentAuctionRound() {
        return currentAuctionRound;
    }

    public List<RoomPlayer> getPlayers() {
        return List.copyOf(players);
    }

    public List<RoomTeamLeader> getLeaders() {
        return List.copyOf(leaders);
    }

    public List<RoomTeamMember> getMembers() {
        return List.copyOf(members);
    }

    public List<RoomBid> getBids() {
        if (currentAuctionRound == null) {
            return List.of();
        }
        return bidHistory.stream().filter(bid -> bid.getRound() == currentAuctionRound).toList();
    }

    public List<RoomBid> bidHistory() {
        return List.copyOf(bidHistory);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public boolean isWaiting() {
        return status == RoomStatus.WAITING;
    }

    public boolean isInProgress() {
        return status == RoomStatus.IN_PROGRESS;
    }

    public boolean isAuction() {
        return mode == TeamBuildingMode.AUCTION;
    }

    public boolean isDraft() {
        return mode == TeamBuildingMode.DRAFT;
    }

    public Room copy() {
        return restore(
                getRoomId(),
                getCode(),
                hostId,
                status,
                mode,
                teamCount,
                teamSize,
                budget,
                draftOrderStrategy,
                currentTurnIndex,
                currentAuctionRound,
                players,
                leaders,
                members,
                bidHistory,
                createdAt,
                updatedAt);
    }

    @PostLoad
    private void validateLoadedState() {
        validateState();
    }

    private Room settleSold(RoomPlayer target, AuctionRoundSettlement settlement) {
        RoomBid winningBid = requireNonNullValue(settlement.getWinningBid(), "낙찰 정산에는 최고 입찰이 필요합니다");
        RoomTeamLeader winner = leaders.stream()
                .filter(leader -> leader.getTeamLeaderId().equals(winningBid.getTeamLeaderId()))
                .findFirst()
                .orElseThrow(RoomException.TeamLeaderNotFoundException::new);

        int leaderMemberCount = (int) members.stream()
                .filter(member -> member.getTeamLeaderId().equals(winningBid.getTeamLeaderId()))
                .count();
        new AuctionRound(winningBid.getRound(), winningBid).requireRosterCapacity(leaderMemberCount, picksPerTeam());

        int assignedCount = members.size();
        advanceAuction(settlement.getNextRound(), settlement.isCompleted());
        target.assign();
        winner.spend(winningBid.getAmount());
        addMember(RoomTeamMember.create(getRoomId(), winningBid.getTeamLeaderId(), target.getName(), assignedCount));
        return this;
    }

    private Room settlePassed(RoomPlayer target, AuctionRoundSettlement settlement) {
        moveAuctionTargetToNextRound(settlement.getNextRound());
        int maxOrder = players.stream().mapToInt(RoomPlayer::getDisplayOrder).max().orElse(target.getDisplayOrder());
        target.moveToBack(maxOrder + 1);
        return this;
    }

    private RoomTeamLeader createTeamLeader(String teamLeaderId, String nickname) {
        Integer remainingBudget = switch (configurationMode()) {
            case AUCTION -> budget;
            case DRAFT -> null;
        };
        return RoomTeamLeader.create(getRoomId(), teamLeaderId, nickname, remainingBudget);
    }

    private void addPlayer(RoomPlayer player) {
        player.attach(this);
        players.add(player);
    }

    private void addLeader(RoomTeamLeader leader) {
        leader.attach(this);
        leaders.add(leader);
    }

    private void addMember(RoomTeamMember member) {
        member.attach(this);
        members.add(member);
    }

    private void addBid(RoomBid bid) {
        bid.attach(this);
        bidHistory.add(bid);
    }

    private void attachGraph() {
        players.forEach(player -> player.attach(this));
        leaders.forEach(leader -> leader.attach(this));
        members.forEach(member -> member.attach(this));
        bidHistory.forEach(bid -> bid.attach(this));
    }

    private TeamBuildingMode configurationMode() {
        return mode;
    }

    private int picksPerTeam() {
        return teamSize - 1;
    }

    private void requireNextAuctionRound(int nextRound) {
        int currentRound = requireCurrentAuctionRound();
        require(nextRound > currentRound, "다음 경매 라운드는 현재보다 커야 합니다");
    }

    private void requireNextDraftTurn(int nextTurnIndex) {
        int currentIndex = requireCurrentTurnIndex();
        require(nextTurnIndex > currentIndex, "다음 드래프트 턴은 현재보다 커야 합니다");
    }

    private void validateState() {
        if (mode == TeamBuildingMode.AUCTION) {
            requireNonNullValue(budget, "경매 방에는 예산이 필요합니다");
            require(draftOrderStrategy == null, "경매 방에는 드래프트 순서 전략이 있으면 안 됩니다");
            return;
        }
        require(budget == null, "드래프트 방에는 예산이 있으면 안 됩니다");
        requireNonNullValue(draftOrderStrategy, "드래프트 방에는 순서 전략이 필요합니다");
    }

    private static List<RoomPlayer> copyPlayers(RoomId roomId, List<RoomPlayer> players) {
        if (players == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(players.stream()
                .map(player -> RoomPlayer.restore(
                        player.getRoomPlayerId(),
                        roomId,
                        player.getName(),
                        player.getStatus(),
                        player.getDisplayOrder(),
                        player.getCreatedAt(),
                        player.getUpdatedAt()))
                .toList());
    }

    private static List<RoomTeamLeader> copyLeaders(RoomId roomId, List<RoomTeamLeader> leaders) {
        if (leaders == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(leaders.stream()
                .map(leader -> RoomTeamLeader.restore(
                        leader.getRoomTeamLeaderId(),
                        roomId,
                        leader.getTeamLeaderId(),
                        leader.getNickname(),
                        leader.getRemainingBudget(),
                        leader.getCreatedAt(),
                        leader.getUpdatedAt()))
                .toList());
    }

    private static List<RoomTeamMember> copyMembers(RoomId roomId, List<RoomTeamMember> members) {
        if (members == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(members.stream()
                .map(member -> RoomTeamMember.restore(
                        member.getRoomTeamMemberId(),
                        roomId,
                        member.getTeamLeaderId(),
                        member.getPlayerName(),
                        member.getAssignOrder(),
                        member.getCreatedAt(),
                        member.getUpdatedAt()))
                .toList());
    }

    private static List<RoomBid> copyBids(RoomId roomId, List<RoomBid> bids) {
        if (bids == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(bids.stream()
                .map(bid -> RoomBid.restore(
                        bid.getRoomBidId(),
                        roomId,
                        bid.getRound(),
                        bid.getTeamLeaderId(),
                        bid.getAmount(),
                        bid.getCreatedAt(),
                        bid.getUpdatedAt()))
                .toList());
    }

    private static String requireText(String value, String message) {
        String normalized = Objects.requireNonNull(value, "value").trim();
        require(!normalized.isEmpty(), message);
        return normalized;
    }

    private static <T> T requireNonNullValue(T value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    private static void checkState(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
