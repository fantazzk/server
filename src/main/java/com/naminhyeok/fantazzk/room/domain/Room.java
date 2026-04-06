package com.naminhyeok.fantazzk.room.domain;

import com.naminhyeok.fantazzk.room.RoomCode;
import com.naminhyeok.fantazzk.room.RoomId;
import com.naminhyeok.fantazzk.room.application.RoomTemplateSpec;
import com.naminhyeok.fantazzk.room.exception.RoomException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.jmolecules.ddd.types.AggregateRoot;
import org.springframework.lang.Nullable;

public final class Room implements AggregateRoot<Room, RoomId> {
    private final RoomId roomId;
    private final RoomCode roomCode;
    private final String hostId;
    private RoomStatus status;
    private final TeamBuildingMode mode;
    private final int teamCount;
    private final int teamSize;
    private final Integer budget;
    private final DraftOrderStrategy draftOrderStrategy;
    private Integer currentTurnIndex;
    private Integer currentAuctionRound;
    private final List<RoomPlayer> players;
    private final List<RoomTeamLeader> leaders;
    private final List<RoomTeamMember> members;
    private final List<RoomBid> bidHistory;
    private final Instant createdAt;
    private final Instant updatedAt;

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
        this.roomId = roomId == null ? RoomId.random() : roomId;
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
        this.players = copyPlayers(this.roomId, players);
        this.leaders = copyLeaders(this.roomId, leaders);
        this.members = copyMembers(this.roomId, members);
        this.bidHistory = copyBids(this.roomId, bidHistory);
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
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
                .map(player -> RoomPlayer.create(room.roomId, player.getName(), player.getDisplayOrder()))
                .forEach(room.players::add);
        room.leaders.add(room.createHostLeader(hostNickname).copy());
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
        leaders.add(leader.copy());
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
        bidHistory.add(RoomBid.create(roomId, currentRound, teamLeaderId, amount));
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
        members.add(RoomTeamMember.create(roomId, teamLeaderId, playerName, assignedCount));
        return this;
    }

    @Override
    public RoomId getId() {
        return roomId;
    }

    public RoomId getRoomId() {
        return roomId;
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
                roomId,
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
        members.add(RoomTeamMember.create(roomId, winningBid.getTeamLeaderId(), target.getName(), assignedCount));
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
        return RoomTeamLeader.create(roomId, teamLeaderId, nickname, remainingBudget);
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
