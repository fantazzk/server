package com.naminhyeok.fantazzk.room;

import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import org.jmolecules.ddd.types.AggregateRoot;

@Getter
@Table(name = "rooms")
public class Room implements AggregateRoot<Room, RoomId> {
    private final RoomId id;
    private final String code;
    private final String hostId;
    private RoomStatus status;
    private final RoomMode mode;
    private final int teamCount;
    private final int teamSize;
    private final Integer budget;
    private final RoomTemplateSpec.DraftOrderStrategy draftOrderStrategy;
    private Integer currentTurnIndex;
    private Integer currentAuctionRound;
    private final List<RoomPlayer> players;
    private final List<RoomTeamLeader> leaders;
    private final List<RoomTeamMember> members;
    private final List<RoomBid> bids;

    Room(
        String code,
        String hostId,
        RoomMode mode,
        int teamCount,
        int teamSize,
        Integer budget,
        RoomTemplateSpec.DraftOrderStrategy draftOrderStrategy
    ) {
        this.id = new RoomId(UUID.randomUUID());
        this.code = code;
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
        RoomTemplateSpec spec
    ) {
        Room room =
            new Room(
                code,
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

        room.leaders.add(new RoomTeamLeader(hostId, hostNickname, spec.budget()));
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

    public void join(String teamLeaderId, String nickname) {
        if (status != RoomStatus.WAITING) {
            throw new IllegalStateException("대기 중인 방에서만 참가할 수 있습니다");
        }
        if (leaders.size() >= teamCount) {
            throw new IllegalStateException("방이 가득 찼습니다");
        }
        leaders.add(new RoomTeamLeader(teamLeaderId, nickname, budget));
    }

    public void start() {
        if (status != RoomStatus.WAITING) {
            throw new IllegalStateException("대기 중인 방에서만 시작할 수 있습니다");
        }
        if (leaders.size() != teamCount) {
            throw new IllegalStateException("모든 팀장 자리가 채워져야 시작할 수 있습니다");
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
            throw new IllegalStateException("진행 중인 방에서만 가능합니다");
        }
        if (mode != RoomMode.AUCTION) {
            throw new IllegalStateException("경매 모드가 아닙니다");
        }
        if (currentAuctionRound == null) {
            throw new IllegalArgumentException("현재 경매 라운드가 없습니다");
        }

        RoomTeamLeader leader =
            leaders.stream()
                .filter(it -> it.getTeamLeaderId().equals(teamLeaderId))
                .findFirst()
                .orElseThrow();

        if (leader.getRemainingBudget() != null && leader.getRemainingBudget() < amount) {
            throw new IllegalArgumentException("예산이 부족합니다");
        }

        bids.stream()
            .filter(it -> it.getRound() == currentAuctionRound)
            .mapToInt(RoomBid::getAmount)
            .max()
            .ifPresent(highest -> {
                if (amount <= highest) {
                    throw new IllegalArgumentException("현재 최고가보다 높아야 합니다");
                }
            });

        RoomBid bid = new RoomBid(currentAuctionRound, teamLeaderId, amount);
        bids.add(bid);
        return bid;
    }

    public AuctionSettlement settleAuction() {
        if (status != RoomStatus.IN_PROGRESS) {
            throw new IllegalStateException("진행 중인 방에서만 가능합니다");
        }
        if (mode != RoomMode.AUCTION) {
            throw new IllegalStateException("경매 모드가 아닙니다");
        }
        if (currentAuctionRound == null) {
            throw new IllegalArgumentException("현재 경매 라운드가 없습니다");
        }

        RoomPlayer target =
            players.stream()
                .filter(it -> it.getStatus() == PlayerStatus.AVAILABLE)
                .min(Comparator.comparingInt(RoomPlayer::getDisplayOrder))
                .orElseThrow();

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
                .orElseThrow();

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
            throw new IllegalStateException("진행 중인 방에서만 가능합니다");
        }
        if (mode != RoomMode.DRAFT) {
            throw new IllegalStateException("드래프트 모드가 아닙니다");
        }
        if (currentTurnIndex == null) {
            throw new IllegalArgumentException("현재 드래프트 턴이 없습니다");
        }

        String currentLeaderId = leaders.get(currentTurnIndex % leaders.size()).getTeamLeaderId();
        if (!currentLeaderId.equals(teamLeaderId)) {
            throw new IllegalStateException("현재 턴이 아닙니다");
        }

        RoomPlayer player =
            players.stream()
                .filter(it -> it.getName().equals(playerName))
                .filter(it -> it.getStatus() == PlayerStatus.AVAILABLE)
                .findFirst()
                .orElseThrow();

        player.assign();
        RoomTeamMember member = new RoomTeamMember(teamLeaderId, playerName, members.size());
        members.add(member);

        currentTurnIndex += 1;
        if (members.size() == teamCount * (teamSize - 1)) {
            status = RoomStatus.COMPLETED;
        }

        return member;
    }
}
