package com.naminhyeok.fantazzk.room.support;

import com.naminhyeok.fantazzk.room.domain.DraftOrderStrategy;
import com.naminhyeok.fantazzk.room.domain.Game;
import com.naminhyeok.fantazzk.room.domain.GameFactory;
import com.naminhyeok.fantazzk.room.domain.GameId;
import com.naminhyeok.fantazzk.room.domain.GameParticipant;
import com.naminhyeok.fantazzk.room.domain.GamePlayer;
import com.naminhyeok.fantazzk.room.domain.GameRules;
import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.RoomMode;
import com.naminhyeok.fantazzk.room.domain.RoomPlayerId;
import com.naminhyeok.fantazzk.room.domain.RoomTemplateSpec;
import com.naminhyeok.fantazzk.room.domain.StartedGameSnapshot;
import com.naminhyeok.fantazzk.room.domain.StartedRoomSnapshot;
import com.naminhyeok.fantazzk.room.domain.TeamLeaderId;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class RoomFixtureBuilder {
    public static final Instant CREATED_AT = Instant.parse("2024-01-01T10:00:00Z");
    public static final String ROOM_CODE = "ROOM01";
    public static final String HOST_ID = "leader-host";
    public static final String HOST_TOKEN = "host-token";
    public static final String GUEST_ID = "leader-guest";
    public static final String GUEST_TOKEN = "guest-token";

    private static final String GAME_TYPE = "LEAGUE_OF_LEGENDS";
    private static final String HOST_NICKNAME = "호스트";
    private static final String GUEST_NICKNAME = "게스트";

    private final RoomMode mode;
    private String code = ROOM_CODE;
    private Instant createdAt = CREATED_AT;
    private int teamCount = 2;
    private int teamSize = 2;
    private Integer budget;
    private int pickBanTime;
    private Integer minBidUnit;
    private DraftOrderStrategy draftOrderStrategy;
    private boolean joined;
    private boolean draftPositionsSelected;
    private boolean started;
    private List<RoomTemplateSpec.Player> players;

    private RoomFixtureBuilder(RoomMode mode) {
        this.mode = mode;
        if (mode == RoomMode.AUCTION) {
            this.budget = 300;
            this.pickBanTime = 30;
            this.minBidUnit = 10;
            this.draftOrderStrategy = null;
        } else {
            this.budget = null;
            this.pickBanTime = 30;
            this.minBidUnit = null;
            this.draftOrderStrategy = DraftOrderStrategy.SNAKE;
        }
    }

    public static RoomFixtureBuilder auction() {
        return new RoomFixtureBuilder(RoomMode.AUCTION);
    }

    public static RoomFixtureBuilder draft() {
        return new RoomFixtureBuilder(RoomMode.DRAFT);
    }

    public RoomFixtureBuilder code(String code) {
        this.code = code;
        return this;
    }

    public RoomFixtureBuilder createdAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public RoomFixtureBuilder teamSize(int teamSize) {
        this.teamSize = teamSize;
        this.players = null;
        return this;
    }

    public RoomFixtureBuilder pickBanTime(int pickBanTime) {
        this.pickBanTime = pickBanTime;
        return this;
    }

    public RoomFixtureBuilder joined() {
        this.joined = true;
        return this;
    }

    public RoomFixtureBuilder draftPositionsSelected() {
        this.joined = true;
        this.draftPositionsSelected = true;
        return this;
    }

    public RoomFixtureBuilder started() {
        this.joined = true;
        this.started = true;
        if (mode == RoomMode.DRAFT) {
            this.draftPositionsSelected = true;
        }
        return this;
    }

    public RoomFixtureBuilder players(List<RoomTemplateSpec.Player> players) {
        this.players = List.copyOf(players);
        return this;
    }

    public Room buildRoom() {
        Room room = Room.createFromTemplate(
            code,
            new TeamLeaderId(HOST_ID),
            HOST_NICKNAME,
            HOST_TOKEN,
            spec(),
            createdAt
        );
        if (joined) {
            room.join(new TeamLeaderId(GUEST_ID), GUEST_NICKNAME, GUEST_TOKEN);
        }
        if (draftPositionsSelected) {
            room.selectDraftPosition(new TeamLeaderId(HOST_ID), 1);
            room.selectDraftPosition(new TeamLeaderId(GUEST_ID), 2);
        }
        if (started) {
            room.start(new TeamLeaderId(HOST_ID), deterministicGameId(room), createdAt);
        }
        return room;
    }

    public StartedRoomSnapshot buildStartedDetails() {
        Room room = started().buildRoom();
        return new StartedRoomSnapshot(room, gameFor(room));
    }

    public static Game gameFor(Room room) {
        return new GameFactory().create(startedGameSnapshotOf(room));
    }

    public static GameId deterministicGameId(Room room) {
        String source = "game:%s".formatted(room.getId().roomId());
        return new GameId(UUID.nameUUIDFromBytes(source.getBytes(StandardCharsets.UTF_8)));
    }

    private RoomTemplateSpec spec() {
        return new RoomTemplateSpec(
            GAME_TYPE,
            mode,
            teamCount,
            teamSize,
            budget,
            pickBanTime,
            minBidUnit,
            draftOrderStrategy,
            players == null ? defaultPlayers() : players
        );
    }

    private List<RoomTemplateSpec.Player> defaultPlayers() {
        List<RoomTemplateSpec.Player> defaultPlayers = new ArrayList<>();
        int playerCount = teamCount * (teamSize - 1);
        String[] positions = {"TOP", "JUNGLE", "MID", "ADC", "SUPPORT"};
        for (int index = 0; index < playerCount; index++) {
            defaultPlayers.add(
                new RoomTemplateSpec.Player(
                    new RoomPlayerId(index),
                    "선수" + (index + 1),
                    positions[index % positions.length],
                    index
                )
            );
        }
        return defaultPlayers;
    }

    private static StartedGameSnapshot startedGameSnapshotOf(Room room) {
        return new StartedGameSnapshot(
            room.getId(),
            room.getCode(),
            room.getStartedGameId(),
            room.getStartedAt(),
            room.getGameType(),
            room.getMode(),
            room.getMode() == RoomMode.AUCTION
                ? GameRules.auction(
                    room.getTeamCount(),
                    room.getTeamSize(),
                    room.getBudget(),
                    room.getPickBanTime(),
                    room.getMinBidUnit()
                )
                : GameRules.draft(
                    room.getTeamCount(),
                    room.getTeamSize(),
                    room.getPickBanTime(),
                    room.getDraftOrderStrategy()
                ),
            room.getLeaders().stream()
                .map(leader -> room.getMode() == RoomMode.AUCTION
                    ? GameParticipant.auction(leader.getId(), leader.getNickname(), leader.getRemainingBudget())
                    : GameParticipant.draft(leader.getId(), leader.getNickname(), leader.getDraftPosition()))
                .toList(),
            room.getPlayers().stream()
                .map(player -> new GamePlayer(player.getId(), player.getName(), player.getPosition(), player.getDisplayOrder()))
                .toList()
        );
    }
}
