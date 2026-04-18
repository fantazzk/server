package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.room.domain.game.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.repository.*;
import com.naminhyeok.fantazzk.room.domain.room.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class RoomApiTestFixtures {
    public static final Instant CREATED_AT = Instant.parse("2026-04-09T00:00:00Z");
    public static final String ROOM_CODE = "ROOM01";
    public static final String GAME_ID = "00000000-0000-0000-0000-000000000201";
    public static final String DRAFT_GAME_ID = "00000000-0000-0000-0000-000000000202";
    public static final String HOST_ID = "host-1";
    public static final String HOST_TOKEN = "host-action-token";
    public static final String GUEST_ID = "guest-1";
    public static final String GUEST_TOKEN = "guest-action-token";

    private RoomApiTestFixtures() {}

    public static JoinableRoomView waitingAuctionJoinableRoomView(String code, Instant createdAt) {
        return JoinableRoomView.from(waitingAuctionRoom(code, createdAt));
    }

    public static JoinableRoomView waitingDraftJoinableRoomView(String code, Instant createdAt) {
        return JoinableRoomView.from(waitingDraftRoom(code, createdAt));
    }

    public static RoomView waitingDraftRoomView() {
        return RoomView.from(waitingDraftRoom());
    }

    public static RoomView selectedDraftPositionRoomView() {
        Room room = waitingDraftRoom();
        room.selectDraftPosition(new TeamLeaderId(GUEST_ID), 2);
        return RoomView.from(room);
    }

    public static RoomView clearedDraftPositionRoomView() {
        Room room = waitingDraftRoom();
        room.clearDraftPosition(new TeamLeaderId(HOST_ID));
        return RoomView.from(room);
    }

    public static RoomView startedAuctionRoomView() {
        return RoomView.from(startedAuctionRoom());
    }

    public static RoomSessionView createdAuctionRoomSessionView() {
        return RoomSessionView.fromHost(waitingAuctionRoom());
    }

    public static RoomSessionView joinedAuctionRoomSessionView() {
        Room room = joinedAuctionRoom();
        return RoomSessionView.from(
            room,
            room.getLeaders().getLast()
        );
    }

    public static GameView startedAuctionGameView() {
        return GameView.from(startedAuctionGame(startedAuctionRoom()));
    }

    public static GameView inProgressAuctionGameView() {
        return GameView.from(inProgressAuctionGame());
    }

    public static GameView inProgressDraftGameView() {
        return GameView.from(inProgressDraftGame());
    }

    static Room waitingAuctionRoom() {
        return waitingAuctionRoom(ROOM_CODE, CREATED_AT);
    }

    static Room waitingAuctionRoom(String code, Instant createdAt) {
        return Room.createFromTemplate(
            code,
            new TeamLeaderId(HOST_ID),
            "호스트",
            HOST_TOKEN,
            new RoomTemplateSpec(
                RoomMode.AUCTION,
                2,
                2,
                300,
                15,
                10,
                null,
                List.of(
                    new RoomTemplateSpec.Player(new RoomPlayerId(0), "선수1", "TOP", 0),
                    new RoomTemplateSpec.Player(new RoomPlayerId(1), "선수2", "JUNGLE", 1)
                )
            ),
            createdAt
        );
    }

    static Room joinedAuctionRoom() {
        Room room = waitingAuctionRoom();
        room.join(new TeamLeaderId(GUEST_ID), "게스트", GUEST_TOKEN);
        return room;
    }

    static Room waitingDraftRoom() {
        return waitingDraftRoom(ROOM_CODE, CREATED_AT);
    }

    static Room waitingDraftRoom(String code, Instant createdAt) {
        Room room =
            Room.createFromTemplate(
                code,
                new TeamLeaderId(HOST_ID),
                "호스트",
                HOST_TOKEN,
                new RoomTemplateSpec(
                    RoomMode.DRAFT,
                    2,
                    2,
                    null,
                    30,
                    null,
                    DraftOrderStrategy.SNAKE,
                    List.of(
                        new RoomTemplateSpec.Player(new RoomPlayerId(0), "선수1", "TOP", 0),
                        new RoomTemplateSpec.Player(new RoomPlayerId(1), "선수2", "JUNGLE", 1)
                    )
                ),
                createdAt
            );
        room.join(new TeamLeaderId(GUEST_ID), "게스트", GUEST_TOKEN);
        room.selectDraftPosition(new TeamLeaderId(HOST_ID), 1);
        return room;
    }

    static Room startedAuctionRoom() {
        Room room = joinedAuctionRoom();
        room.start(new TeamLeaderId(HOST_ID), new GameId(UUID.fromString(GAME_ID)), CREATED_AT);
        return room;
    }

    static Room inProgressAuctionRoom() {
        Room room =
            Room.createFromTemplate(
                ROOM_CODE,
                new TeamLeaderId(HOST_ID),
                "호스트",
                HOST_TOKEN,
                new RoomTemplateSpec(
                    RoomMode.AUCTION,
                    2,
                    3,
                    300,
                    15,
                    10,
                    null,
                    List.of(
                        new RoomTemplateSpec.Player(new RoomPlayerId(0), "선수1", "TOP", 0),
                        new RoomTemplateSpec.Player(new RoomPlayerId(1), "선수2", "JUNGLE", 1),
                        new RoomTemplateSpec.Player(new RoomPlayerId(2), "선수3", "MID", 2),
                        new RoomTemplateSpec.Player(new RoomPlayerId(3), "선수4", "ADC", 3)
                    )
                ),
                CREATED_AT
            );
        room.join(new TeamLeaderId(GUEST_ID), "게스트", GUEST_TOKEN);
        room.start(new TeamLeaderId(HOST_ID), new GameId(UUID.fromString(GAME_ID)), CREATED_AT);
        return room;
    }

    private static AuctionGame inProgressAuctionGame() {
        Room room =
            Room.createFromTemplate(
                ROOM_CODE,
                new TeamLeaderId(HOST_ID),
                "호스트",
                HOST_TOKEN,
                new RoomTemplateSpec(
                    RoomMode.AUCTION,
                    2,
                    3,
                    300,
                    15,
                    10,
                    null,
                    List.of(
                        new RoomTemplateSpec.Player(new RoomPlayerId(0), "선수1", "TOP", 0),
                        new RoomTemplateSpec.Player(new RoomPlayerId(1), "선수2", "JUNGLE", 1),
                        new RoomTemplateSpec.Player(new RoomPlayerId(2), "선수3", "MID", 2),
                        new RoomTemplateSpec.Player(new RoomPlayerId(3), "선수4", "ADC", 3)
                    )
                ),
                CREATED_AT
            );
        room.join(new TeamLeaderId(GUEST_ID), "게스트", GUEST_TOKEN);
        StartedGameSnapshot snapshot = room.start(new TeamLeaderId(HOST_ID), new GameId(UUID.fromString(GAME_ID)), CREATED_AT);
        AuctionGame game = (AuctionGame) new GameFactory().create(snapshot);
        game.placeBid(new TeamLeaderId(HOST_ID), 100, CREATED_AT.plusSeconds(1));
        game.settleAuction(CREATED_AT.plusSeconds(16));
        return game;
    }

    static Room inProgressDraftRoom() {
        Room room =
            Room.createFromTemplate(
                ROOM_CODE,
                new TeamLeaderId(HOST_ID),
                "호스트",
                HOST_TOKEN,
                new RoomTemplateSpec(
                    RoomMode.DRAFT,
                    2,
                    3,
                    null,
                    30,
                    null,
                    DraftOrderStrategy.SNAKE,
                    List.of(
                        new RoomTemplateSpec.Player(new RoomPlayerId(0), "선수1", "TOP", 0),
                        new RoomTemplateSpec.Player(new RoomPlayerId(1), "선수2", "JUNGLE", 1),
                        new RoomTemplateSpec.Player(new RoomPlayerId(2), "선수3", "MID", 2),
                        new RoomTemplateSpec.Player(new RoomPlayerId(3), "선수4", "ADC", 3)
                    )
                ),
                CREATED_AT
            );
        room.join(new TeamLeaderId(GUEST_ID), "게스트", GUEST_TOKEN);
        room.selectDraftPosition(new TeamLeaderId(HOST_ID), 1);
        room.selectDraftPosition(new TeamLeaderId(GUEST_ID), 2);
        room.start(new TeamLeaderId(HOST_ID), new GameId(UUID.fromString(DRAFT_GAME_ID)), CREATED_AT);
        return room;
    }

    private static DraftGame inProgressDraftGame() {
        Room room =
            Room.createFromTemplate(
                ROOM_CODE,
                new TeamLeaderId(HOST_ID),
                "호스트",
                HOST_TOKEN,
                new RoomTemplateSpec(
                    RoomMode.DRAFT,
                    2,
                    3,
                    null,
                    30,
                    null,
                    DraftOrderStrategy.SNAKE,
                    List.of(
                        new RoomTemplateSpec.Player(new RoomPlayerId(0), "선수1", "TOP", 0),
                        new RoomTemplateSpec.Player(new RoomPlayerId(1), "선수2", "JUNGLE", 1),
                        new RoomTemplateSpec.Player(new RoomPlayerId(2), "선수3", "MID", 2),
                        new RoomTemplateSpec.Player(new RoomPlayerId(3), "선수4", "ADC", 3)
                    )
                ),
                CREATED_AT
            );
        room.join(new TeamLeaderId(GUEST_ID), "게스트", GUEST_TOKEN);
        room.selectDraftPosition(new TeamLeaderId(HOST_ID), 1);
        room.selectDraftPosition(new TeamLeaderId(GUEST_ID), 2);
        StartedGameSnapshot snapshot = room.start(new TeamLeaderId(HOST_ID), new GameId(UUID.fromString(DRAFT_GAME_ID)), CREATED_AT);
        DraftGame game = (DraftGame) new GameFactory().create(snapshot);
        game.pick(new TeamLeaderId(HOST_ID), "선수1");
        game.pick(new TeamLeaderId(GUEST_ID), "선수2");
        return game;
    }

    private static AuctionGame startedAuctionGame(Room room) {
        StartedGameSnapshot snapshot = new StartedGameSnapshot(
            room.getId(),
            room.getCode(),
            room.getStartedGameId(),
            CREATED_AT,
            room.getMode(),
            GameRules.auction(
                room.getTeamCount(),
                room.getTeamSize(),
                room.getBudget(),
                room.getPickBanTime(),
                room.getMinBidUnit(),
                room.getPositionLimit()
            ),
            room.getLeaders().stream()
                .map(leader -> room.getMode() == RoomMode.AUCTION
                    ? new StartedAuctionParticipant(leader.getId(), leader.getNickname(), leader.getRemainingBudget())
                    : new StartedDraftParticipant(leader.getId(), leader.getNickname(), leader.getDraftPosition()))
                .toList(),
            room.getPlayers().stream()
                .map(player -> new StartedGamePlayer(player.getId(), player.getName(), player.getPosition(), player.getDisplayOrder()))
                .toList()
        );
        return (AuctionGame) new GameFactory().create(snapshot);
    }
}
