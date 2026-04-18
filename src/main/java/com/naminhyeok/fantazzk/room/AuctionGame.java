package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.room.domain.event.AuctionSettled;
import com.naminhyeok.fantazzk.room.domain.event.BidPlaced;
import com.naminhyeok.fantazzk.room.domain.event.RoomStarted;
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
@DiscriminatorValue("AUCTION")
class AuctionGame extends Game {
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "game_participant", joinColumns = @JoinColumn(name = "participants_game_id"))
    @OrderColumn(name = "participant_order")
    private final List<AuctionParticipant> participants;
    @Column(name = "current_round")
    private int currentRound;
    @Column(name = "current_round_ends_at")
    private Instant currentRoundEndsAt;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "game_auction_bid", joinColumns = @JoinColumn(name = "bids_game_id"))
    @OrderColumn(name = "bid_order")
    private final List<AuctionBid> bids;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "game_auction_member", joinColumns = @JoinColumn(name = "members_game_id"))
    @OrderColumn(name = "member_order")
    private final List<RosterMember> members;

    protected AuctionGame() {
        this.participants = new ArrayList<>();
        this.currentRound = 0;
        this.currentRoundEndsAt = null;
        this.bids = new ArrayList<>();
        this.members = new ArrayList<>();
    }

    AuctionGame(
        GameId id,
        RoomId roomId,
        String roomCode,
        Instant startedAt,
        GameRules rules,
        List<AuctionParticipant> participants,
        List<GamePlayer> playerPool,
        int currentRound,
        Instant currentRoundEndsAt
    ) {
        super(id, roomId, roomCode, startedAt, GameStatus.IN_PROGRESS, rules, playerPool);
        this.participants = new ArrayList<>(participants);
        this.currentRound = currentRound;
        this.currentRoundEndsAt = currentRoundEndsAt;
        this.bids = new ArrayList<>();
        this.members = new ArrayList<>();
        if (currentRoundEndsAt != null) {
            registerEvent(new RoomStarted(roomCode, currentRoundEndsAt));
        }
    }

    @Override
    GameRules getRules() {
        return GameRules.auction(
            getTeamCount(),
            getTeamSize(),
            getBudget(),
            getPickBanTime(),
            getMinBidUnit(),
            getPositionLimit()
        );
    }

    @Override
    List<GameParticipant> getParticipants() {
        return participants.stream().map(GameParticipant.class::cast).toList();
    }

    AuctionBid placeBid(TeamLeaderId teamLeaderId, int amount, Instant now) {
        if (getStatus() != GameStatus.IN_PROGRESS) {
            throw CoreException.of(RoomErrorType.ROOM_PLAY_REQUIRES_IN_PROGRESS);
        }
        if (amount <= 0) {
            throw CoreException.of(RoomErrorType.ROOM_BID_AMOUNT_NOT_POSITIVE);
        }
        if (currentRound <= 0) {
            throw RoomStateInvalidException.auctionRoundMissing();
        }
        if (currentRoundEndsAt == null || !now.isBefore(currentRoundEndsAt)) {
            throw CoreException.of(RoomErrorType.ROOM_BID_REQUIRES_OPEN_ROUND);
        }

        AuctionParticipant leader = findParticipant(teamLeaderId, RoomErrorType.ROOM_BIDDER_NOT_FOUND);
        GameParticipant.AuctionState bidder = leader.auctionState();
        GamePlayer target = requireCurrentAuctionTarget();
        validateAuctionPositionLimit(teamLeaderId, target);

        if (bidder.remainingBudget() < amount) {
            throw CoreException.of(RoomErrorType.ROOM_BID_BUDGET_EXCEEDED);
        }

        Integer highestBidAmount =
            bids.stream()
                .filter(it -> it.round() == currentRound)
                .map(AuctionBid::amount)
                .max(Integer::compareTo)
                .orElse(null);

        if (highestBidAmount == null) {
            if (amount < getRules().auctionRules().minBidUnit()) {
                throw CoreException.of(RoomErrorType.ROOM_BID_MIN_UNIT_NOT_MET);
            }
        } else {
            if (amount <= highestBidAmount) {
                throw CoreException.of(RoomErrorType.ROOM_BID_TOO_LOW);
            }
            if (amount < highestBidAmount + getRules().auctionRules().minBidUnit()) {
                throw CoreException.of(RoomErrorType.ROOM_BID_MIN_UNIT_NOT_MET);
            }
        }

        BidSequence nextSequence =
            new BidSequence(
                bids.stream()
                    .filter(it -> it.round() == currentRound)
                    .map(AuctionBid::sequence)
                    .mapToInt(BidSequence::value)
                    .max()
                    .orElse(0) + 1
            );
        AuctionBid bid = new AuctionBid(currentRound, nextSequence, teamLeaderId, amount);
        bids.add(bid);
        currentRoundEndsAt = now.plusSeconds(getPickBanTime());
        registerEvent(new BidPlaced(getRoomCode(), teamLeaderId.value(), amount, currentRound, currentRoundEndsAt));
        return bid;
    }

    AuctionSettlement settleAuction(Instant now) {
        if (getStatus() != GameStatus.IN_PROGRESS) {
            throw CoreException.of(RoomErrorType.ROOM_PLAY_REQUIRES_IN_PROGRESS);
        }
        if (currentRound <= 0) {
            throw RoomStateInvalidException.auctionRoundMissing();
        }
        if (currentRoundEndsAt == null) {
            throw RoomStateInvalidException.auctionRoundMissing();
        }
        if (now.isBefore(currentRoundEndsAt)) {
            throw CoreException.of(RoomErrorType.ROOM_AUCTION_ROUND_NOT_ENDED);
        }

        GamePlayer target = requireCurrentAuctionTarget();
        AuctionBid winningBid = currentWinningBid();
        if (winningBid == null) {
            moveCurrentTargetToBack(target);
            advanceRound(now);
            AuctionSettlement settlement = new AuctionSettlement(target.name(), AuctionOutcome.PASSED);
            registerEvent(new AuctionSettled(getRoomCode(), settlement.outcome().name(), currentRoundEndsAt));
            return settlement;
        }

        AuctionParticipant winner = findParticipant(winningBid.teamLeaderId(), RoomErrorType.ROOM_BIDDER_NOT_FOUND);
        validateAuctionPositionLimit(winner.teamLeaderId(), target);
        spend(winner.teamLeaderId(), winningBid.amount());
        members.add(new RosterMember(winningBid.teamLeaderId(), target.name(), members.size()));

        if (members.size() == getTeamCount() * (getTeamSize() - 1)) {
            changeStatus(GameStatus.COMPLETED);
            currentRoundEndsAt = null;
        } else {
            advanceRound(now);
        }
        AuctionSettlement settlement = new AuctionSettlement(target.name(), AuctionOutcome.SOLD);
        registerEvent(new AuctionSettled(getRoomCode(), settlement.outcome().name(), currentRoundEndsAt));
        return settlement;
    }

    GamePlayer currentAuctionTarget() {
        if (getStatus() != GameStatus.IN_PROGRESS || currentRound <= 0) {
            return null;
        }
        return mutablePlayerPool().stream().filter(this::isAvailable).findFirst().orElse(null);
    }

    AuctionBid currentWinningBid() {
        if (getStatus() != GameStatus.IN_PROGRESS || currentRound <= 0) {
            return null;
        }
        return bids.stream()
            .filter(it -> it.round() == currentRound)
            .max(Comparator.comparingInt(AuctionBid::amount))
            .orElse(null);
    }

    int currentBidCount() {
        if (getStatus() != GameStatus.IN_PROGRESS || currentRound <= 0) {
            return 0;
        }
        return (int) bids.stream().filter(it -> it.round() == currentRound).count();
    }

    boolean isDue(Instant now) {
        return getStatus() == GameStatus.IN_PROGRESS && currentRoundEndsAt != null && !currentRoundEndsAt.isAfter(now);
    }

    private GamePlayer requireCurrentAuctionTarget() {
        return mutablePlayerPool().stream().filter(this::isAvailable).findFirst().orElseThrow(RoomStateInvalidException::auctionTargetMissing);
    }

    private boolean isAvailable(GamePlayer player) {
        return members.stream().noneMatch(member -> member.playerName().equals(player.name()));
    }

    private void moveCurrentTargetToBack(GamePlayer target) {
        List<GamePlayer> players = mutablePlayerPool();
        players.remove(target);
        players.add(target);
    }

    private void advanceRound(Instant now) {
        currentRound += 1;
        currentRoundEndsAt = now.plusSeconds(getPickBanTime());
    }

    private void spend(TeamLeaderId leaderId, int amount) {
        List<AuctionParticipant> participants = mutableParticipants();
        for (int index = 0; index < participants.size(); index++) {
            AuctionParticipant participant = participants.get(index);
            if (!participant.teamLeaderId().equals(leaderId)) {
                continue;
            }
            int remainingBudget = participant.auctionState().remainingBudget();
            if (remainingBudget < amount) {
                throw RoomStateInvalidException.auctionWinnerBudgetExceeded(leaderId, remainingBudget, amount);
            }
            participants.set(index, participant.withRemainingBudget(remainingBudget - amount));
            return;
        }
        throw RoomStateInvalidException.auctionWinnerMissing(leaderId);
    }

    private void validateAuctionPositionLimit(TeamLeaderId leaderId, GamePlayer target) {
        Integer positionLimit = getRules().auctionRules().positionLimit();
        if (positionLimit == null) {
            return;
        }

        long assignedCount =
            members.stream()
                .filter(member -> member.teamLeaderId().equals(leaderId))
                .map(this::findAssignedPlayerPosition)
                .filter(target.position()::equals)
                .count();
        if (assignedCount >= positionLimit) {
            throw CoreException.of(RoomErrorType.ROOM_AUCTION_POSITION_LIMIT_EXCEEDED);
        }
    }

    private String findAssignedPlayerPosition(RosterMember member) {
        return mutablePlayerPool().stream()
            .filter(player -> player.name().equals(member.playerName()))
            .findFirst()
            .map(GamePlayer::position)
            .orElse(null);
    }

    private List<AuctionParticipant> mutableParticipants() {
        return participants;
    }

    private AuctionParticipant findParticipant(TeamLeaderId teamLeaderId, RoomErrorType errorType) {
        return mutableParticipants().stream()
            .filter(participant -> participant.teamLeaderId().equals(teamLeaderId))
            .findFirst()
            .orElseThrow(() -> CoreException.of(errorType));
    }
}
