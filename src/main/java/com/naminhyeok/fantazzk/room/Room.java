package com.naminhyeok.fantazzk.room;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;

@Getter
public class Room implements AggregateRoot<Room, Room.RoomId> {
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

    public record RoomId(UUID roomId) implements Identifier {
    }
}
