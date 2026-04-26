package com.naminhyeok.fantazzk.room.support;

import com.naminhyeok.fantazzk.room.domain.AuctionGame;
import com.naminhyeok.fantazzk.room.domain.DraftOrderStrategy;
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

    public static Room waitingAuctionRoom() {
        return waitingAuctionRoom(ROOM_CODE, CREATED_AT);
    }

    public static Room waitingAuctionRoom(String code, Instant createdAt) {
        return Room.createFromTemplate(
            code,
            new TeamLeaderId(HOST_ID),
            "호스트",
            HOST_TOKEN,
            new RoomTemplateSpec(
                "LEAGUE_OF_LEGENDS",
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

    public static Room joinedAuctionRoom() {
        Room room = waitingAuctionRoom();
        room.join(new TeamLeaderId(GUEST_ID), "게스트", GUEST_TOKEN);
        return room;
    }

    public static Room waitingDraftRoom() {
        return waitingDraftRoom(ROOM_CODE, CREATED_AT);
    }

    public static Room waitingDraftRoom(String code, Instant createdAt) {
        Room room =
            Room.createFromTemplate(
                code,
                new TeamLeaderId(HOST_ID),
                "호스트",
                HOST_TOKEN,
                new RoomTemplateSpec(
                    "LEAGUE_OF_LEGENDS",
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

    public static Room startedAuctionRoom() {
        Room room = joinedAuctionRoom();
        room.start(new TeamLeaderId(HOST_ID), new GameId(UUID.fromString(GAME_ID)), CREATED_AT);
        return room;
    }

    public static StartedRoomSnapshot startedAuctionDetails() {
        Room room = startedAuctionRoom();
        return new StartedRoomSnapshot(room, startedAuctionGame(room));
    }

    public static StartedRoomSnapshot inProgressAuctionDetails() {
        Room room =
            Room.createFromTemplate(
                ROOM_CODE,
                new TeamLeaderId(HOST_ID),
                "호스트",
                HOST_TOKEN,
                new RoomTemplateSpec(
                    "LEAGUE_OF_LEGENDS",
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
        return new StartedRoomSnapshot(room, game);
    }

    private static AuctionGame startedAuctionGame(Room room) {
        StartedGameSnapshot snapshot = new StartedGameSnapshot(
            room.getId(),
            room.getCode(),
            room.getStartedGameId(),
            CREATED_AT,
            room.getGameType(),
            room.getMode(),
            GameRules.auction(
                room.getTeamCount(),
                room.getTeamSize(),
                room.getBudget(),
                room.getPickBanTime(),
                room.getMinBidUnit()
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
        return (AuctionGame) new GameFactory().create(snapshot);
    }
}
