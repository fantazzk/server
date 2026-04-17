package com.naminhyeok.fantazzk.auction;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class AuctionRoom {
    private final AuctionRoomId id;
    private final String code;
    private final Instant createdAt;
    private final String hostLeaderId;
    private final int teamCount;
    private final int teamSize;
    private final Integer budget;
    private final int pickBanTime;
    private final Integer minBidUnit;
    private final Integer positionLimit;
    private final List<AuctionPlayer> players;
    private final List<AuctionTeamLeader> leaders;
    private final List<AuctionTeamMember> members;
    private final List<AuctionBid> bids;
    private AuctionRoomStatus status;
    private Integer currentAuctionRound;
    private Instant currentAuctionRoundEndsAt;

    private AuctionRoom(
        String code,
        Instant createdAt,
        String hostLeaderId,
        int teamCount,
        int teamSize,
        Integer budget,
        int pickBanTime,
        Integer minBidUnit,
        Integer positionLimit
    ) {
        this.id = new AuctionRoomId(UUID.randomUUID());
        this.code = code;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.hostLeaderId = Objects.requireNonNull(hostLeaderId, "hostLeaderId must not be null");
        this.teamCount = teamCount;
        this.teamSize = teamSize;
        this.budget = budget;
        this.pickBanTime = pickBanTime;
        this.minBidUnit = minBidUnit;
        this.positionLimit = positionLimit;
        this.players = new ArrayList<>();
        this.leaders = new ArrayList<>();
        this.members = new ArrayList<>();
        this.bids = new ArrayList<>();
        this.status = AuctionRoomStatus.WAITING;
        this.currentAuctionRound = null;
        this.currentAuctionRoundEndsAt = null;
    }

    public AuctionRoomId getId() {
        return id;
    }

    public static AuctionRoom create(
        String code,
        String hostLeaderId,
        String hostNickname,
        Instant createdAt,
        AuctionRoomSetup setup
    ) {
        Objects.requireNonNull(hostNickname, "hostNickname must not be null");
        Objects.requireNonNull(setup, "setup must not be null");
        AuctionRoom room =
            new AuctionRoom(
                code,
                createdAt,
                hostLeaderId,
                setup.teamCount(),
                setup.teamSize(),
                setup.budget(),
                setup.pickBanTime(),
                setup.minBidUnit(),
                setup.positionLimit()
            );
        int requiredPlayerCount = setup.teamCount() * (setup.teamSize() - 1);
        if (setup.players().size() != requiredPlayerCount) {
            throw new IllegalArgumentException("선수 수는 정확히 " + requiredPlayerCount + "명이어야 합니다");
        }
        setup.players().stream()
            .sorted(Comparator.comparingInt(AuctionPlayerSeed::displayOrder))
            .map(player -> new AuctionPlayer(player.playerId(), player.name(), player.position(), player.displayOrder()))
            .forEach(room.players::add);
        room.leaders.add(new AuctionTeamLeader(hostLeaderId, hostNickname.trim(), setup.budget()));
        return room;
    }

    static AuctionRoom restore(AuctionRoomSnapshot snapshot) {
        AuctionRoom room =
            new AuctionRoom(
                snapshot.code(),
                snapshot.createdAt(),
                snapshot.hostLeaderId(),
                snapshot.teamCount(),
                snapshot.teamSize(),
                snapshot.budget(),
                snapshot.pickBanTime(),
                snapshot.minBidUnit(),
                snapshot.positionLimit()
            );
        room.leaders.addAll(snapshot.leaders().stream()
            .map(leader -> new AuctionTeamLeader(leader.leaderId(), leader.nickname(), leader.remainingBudget()))
            .toList());
        room.players.addAll(snapshot.players().stream()
            .map(player -> new AuctionPlayer(
                player.playerId(),
                player.name(),
                player.position(),
                player.displayOrder(),
                player.status()
            ))
            .toList());
        room.members.addAll(snapshot.members().stream()
            .map(member -> new AuctionTeamMember(member.leaderId(), member.playerId(), member.sequence()))
            .toList());
        room.bids.addAll(snapshot.bids().stream()
            .map(bid -> new AuctionBid(bid.round(), bid.sequence(), bid.leaderId(), bid.amount()))
            .toList());
        room.status = snapshot.status();
        room.currentAuctionRound = snapshot.currentAuctionRound();
        room.currentAuctionRoundEndsAt = snapshot.currentAuctionRoundEndsAt();
        return room;
    }

    void addLeader(String leaderId, String nickname) {
        requireWaiting();
        if (leaders.size() >= teamCount) {
            throw AuctionRoomException.leaderLimitReached();
        }

        String normalizedNickname = normalizeNickname(nickname);
        boolean taken =
            leaders.stream()
                .map(AuctionTeamLeader::nickname)
                .map(this::normalizeNickname)
                .anyMatch(normalizedNickname::equals);
        if (taken) {
            throw AuctionRoomException.nicknameTaken(nickname);
        }

        leaders.add(new AuctionTeamLeader(leaderId, nickname.trim(), budget));
    }

    void start(String callerLeaderId, Instant now) {
        requireWaiting();
        if (!hostLeaderId.equals(callerLeaderId)) {
            throw AuctionRoomException.hostForbidden(callerLeaderId);
        }
        if (leaders.size() != teamCount) {
            throw AuctionRoomException.leadersNotFull();
        }

        status = AuctionRoomStatus.IN_PROGRESS;
        currentAuctionRound = 1;
        currentAuctionRoundEndsAt = now.plusSeconds(pickBanTime);
    }

    AuctionBid placeBid(String leaderId, int amount, Instant now) {
        requireInProgress();
        requireCurrentRound();
        if (currentAuctionRoundEndsAt != null && !now.isBefore(currentAuctionRoundEndsAt)) {
            throw AuctionRoomException.roundNotEnded();
        }
        if (amount <= 0) {
            throw AuctionRoomException.amountNotPositive();
        }

        AuctionTeamLeader leader = findLeader(leaderId);
        AuctionPlayer target = requireCurrentTarget();
        validateAuctionPositionLimit(leaderId, target);

        if (leader.remainingBudget() != null && leader.remainingBudget() < amount) {
            throw AuctionRoomException.budgetExceeded();
        }

        Integer highestBidAmount = currentHighestBidAmount();
        if (highestBidAmount == null) {
            if (minBidUnit != null && amount < minBidUnit) {
                throw AuctionRoomException.minUnitNotMet();
            }
        } else {
            if (amount <= highestBidAmount) {
                throw AuctionRoomException.tooLow();
            }
            if (minBidUnit != null && amount < highestBidAmount + minBidUnit) {
                throw AuctionRoomException.minUnitNotMet();
            }
        }

        int sequence = currentBidCount() + 1;
        AuctionBid bid = new AuctionBid(currentAuctionRound, sequence, leaderId, amount);
        bids.add(bid);
        currentAuctionRoundEndsAt = now.plusSeconds(pickBanTime);
        return bid;
    }

    AuctionSettlement settle(Instant now) {
        requireInProgress();
        requireCurrentRound();
        if (currentAuctionRoundEndsAt == null) {
            throw AuctionRoomException.roundMissing();
        }
        if (now.isBefore(currentAuctionRoundEndsAt)) {
            throw AuctionRoomException.roundNotEnded();
        }

        AuctionPlayer target = requireCurrentTarget();
        AuctionBid winningBid = currentWinningBid();

        if (winningBid == null) {
            int maxOrder = players.stream().mapToInt(AuctionPlayer::displayOrder).max().orElse(0);
            target.moveToBack(maxOrder + 1);
            currentAuctionRound += 1;
            currentAuctionRoundEndsAt = now.plusSeconds(pickBanTime);
            return new AuctionSettlement(target.playerId(), target.name(), AuctionOutcome.PASSED);
        }

        AuctionTeamLeader winner = findLeader(winningBid.leaderId());
        validateAuctionPositionLimit(winner.leaderId(), target);
        target.assign();
        winner.spend(winningBid.amount());
        members.add(new AuctionTeamMember(winner.leaderId(), target.playerId(), members.size()));

        if (members.size() == teamCount * (teamSize - 1)) {
            status = AuctionRoomStatus.COMPLETED;
            currentAuctionRoundEndsAt = null;
        } else {
            currentAuctionRound += 1;
            currentAuctionRoundEndsAt = now.plusSeconds(pickBanTime);
        }

        return new AuctionSettlement(target.playerId(), target.name(), AuctionOutcome.SOLD);
    }

    AuctionRoomState readState() {
        if (status == AuctionRoomStatus.WAITING) {
            return new AuctionRoomState(
                code,
                status,
                leaderStates(),
                playerStates(),
                memberStates(),
                null,
                null,
                null,
                null,
                null,
                0
            );
        }

        return new AuctionRoomState(
            code,
            status,
            leaderStates(),
            playerStates(),
            memberStates(),
            currentAuctionRound,
            currentAuctionRoundEndsAt,
            currentTargetSnapshot(),
            currentHighestBidAmount(),
            currentWinningBid() == null ? null : currentWinningBid().leaderId(),
            currentBidCount()
        );
    }

    private List<AuctionRoomState.Leader> leaderStates() {
        return leaders.stream()
            .map(leader -> new AuctionRoomState.Leader(leader.leaderId(), leader.nickname(), leader.remainingBudget()))
            .toList();
    }

    private List<AuctionRoomState.Player> playerStates() {
        return players.stream()
            .map(player -> new AuctionRoomState.Player(
                player.playerId(),
                player.name(),
                player.position(),
                player.displayOrder(),
                !player.available()
            ))
            .toList();
    }

    private List<AuctionRoomState.Member> memberStates() {
        return members.stream()
            .map(member -> new AuctionRoomState.Member(member.leaderId(), member.playerId(), member.sequence()))
            .toList();
    }

    AuctionRoomSnapshot snapshot() {
        return new AuctionRoomSnapshot(
            code,
            createdAt,
            hostLeaderId,
            teamCount,
            teamSize,
            budget,
            pickBanTime,
            minBidUnit,
            positionLimit,
            status,
            currentAuctionRound,
            currentAuctionRoundEndsAt,
            leaders.stream()
                .map(leader -> new AuctionRoomSnapshot.Leader(leader.leaderId(), leader.nickname(), leader.remainingBudget()))
                .toList(),
            players.stream()
                .map(player -> new AuctionRoomSnapshot.Player(
                    player.playerId(),
                    player.name(),
                    player.position(),
                    player.displayOrder(),
                    player.available() ? AuctionPlayerStatus.AVAILABLE : AuctionPlayerStatus.ASSIGNED
                ))
                .toList(),
            members.stream()
                .map(member -> new AuctionRoomSnapshot.Member(member.leaderId(), member.playerId(), member.sequence()))
                .toList(),
            bids.stream()
                .map(bid -> new AuctionRoomSnapshot.Bid(bid.round(), bid.sequence(), bid.leaderId(), bid.amount()))
                .toList()
        );
    }

    private AuctionTarget currentTargetSnapshot() {
        AuctionPlayer target = currentTargetOrNull();
        return target == null ? null : new AuctionTarget(target.playerId(), target.name(), target.position());
    }

    private AuctionPlayer currentTargetOrNull() {
        if (status != AuctionRoomStatus.IN_PROGRESS || currentAuctionRound == null) {
            return null;
        }
        return players.stream()
            .filter(AuctionPlayer::available)
            .min(Comparator.comparingInt(AuctionPlayer::displayOrder))
            .orElse(null);
    }

    private AuctionPlayer requireCurrentTarget() {
        AuctionPlayer target = currentTargetOrNull();
        if (target == null) {
            throw AuctionRoomException.targetMissing();
        }
        return target;
    }

    private Integer currentHighestBidAmount() {
        if (status != AuctionRoomStatus.IN_PROGRESS || currentAuctionRound == null) {
            return null;
        }
        return bids.stream()
            .filter(bid -> bid.round() == currentAuctionRound)
            .map(AuctionBid::amount)
            .max(Integer::compareTo)
            .orElse(null);
    }

    private AuctionBid currentWinningBid() {
        if (status != AuctionRoomStatus.IN_PROGRESS || currentAuctionRound == null) {
            return null;
        }
        return bids.stream()
            .filter(bid -> bid.round() == currentAuctionRound)
            .max(Comparator.comparingInt(AuctionBid::amount))
            .orElse(null);
    }

    private int currentBidCount() {
        if (status != AuctionRoomStatus.IN_PROGRESS || currentAuctionRound == null) {
            return 0;
        }
        return (int) bids.stream().filter(bid -> bid.round() == currentAuctionRound).count();
    }

    private AuctionTeamLeader findLeader(String leaderId) {
        return leaders.stream()
            .filter(leader -> leader.leaderId().equals(leaderId))
            .findFirst()
            .orElseThrow(() -> AuctionRoomException.bidderMissing(leaderId));
    }

    private void validateAuctionPositionLimit(String leaderId, AuctionPlayer target) {
        if (positionLimit == null) {
            return;
        }

        long assignedCount =
            members.stream()
                .filter(member -> member.leaderId().equals(leaderId))
                .map(this::findAssignedPlayerPosition)
                .filter(target.position()::equals)
                .count();
        if (assignedCount >= positionLimit) {
            throw AuctionRoomException.positionLimitExceeded();
        }
    }

    private String findAssignedPlayerPosition(AuctionTeamMember member) {
        return findPlayer(member.playerId()).position();
    }

    private AuctionPlayer findPlayer(int playerId) {
        return players.stream()
            .filter(player -> player.playerId() == playerId)
            .findFirst()
            .orElseThrow(() -> AuctionRoomException.playerMissing(playerId));
    }

    private void requireWaiting() {
        if (status != AuctionRoomStatus.WAITING) {
            throw AuctionRoomException.roomNotWaiting();
        }
    }

    private void requireInProgress() {
        if (status != AuctionRoomStatus.IN_PROGRESS) {
            throw AuctionRoomException.roomNotInProgress();
        }
    }

    private void requireCurrentRound() {
        if (currentAuctionRound == null) {
            throw AuctionRoomException.roundMissing();
        }
    }

    private String normalizeNickname(String nickname) {
        return nickname.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
