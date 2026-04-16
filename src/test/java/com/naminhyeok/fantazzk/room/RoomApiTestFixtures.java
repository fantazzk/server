package com.naminhyeok.fantazzk.room;

import java.time.Instant;
import java.util.List;

final class RoomApiTestFixtures {
    static final Instant CREATED_AT = Instant.parse("2026-04-09T00:00:00Z");
    static final String ROOM_CODE = "ROOM01";
    static final String HOST_ID = "host-1";
    static final String HOST_TOKEN = "host-action-token";
    static final String GUEST_ID = "guest-1";
    static final String GUEST_TOKEN = "guest-action-token";

    private RoomApiTestFixtures() {}

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
                RoomTemplateSpec.Mode.AUCTION,
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
                    RoomTemplateSpec.Mode.DRAFT,
                    2,
                    2,
                    null,
                    30,
                    null,
                    RoomTemplateSpec.DraftOrderStrategy.SNAKE,
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
        room.start(new TeamLeaderId(HOST_ID), CREATED_AT);
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
                    RoomTemplateSpec.Mode.AUCTION,
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
        room.start(new TeamLeaderId(HOST_ID), CREATED_AT);
        room.placeBid(new TeamLeaderId(HOST_ID), 100, CREATED_AT.plusSeconds(1));
        room.settleAuction(CREATED_AT.plusSeconds(16));
        return room;
    }

    static Room inProgressDraftRoom() {
        Room room =
            Room.createFromTemplate(
                ROOM_CODE,
                new TeamLeaderId(HOST_ID),
                "호스트",
                HOST_TOKEN,
                new RoomTemplateSpec(
                    RoomTemplateSpec.Mode.DRAFT,
                    2,
                    3,
                    null,
                    30,
                    null,
                    RoomTemplateSpec.DraftOrderStrategy.SNAKE,
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
        room.start(new TeamLeaderId(HOST_ID), CREATED_AT);
        room.pick(new TeamLeaderId(HOST_ID), new RoomPlayerId(0));
        room.pick(new TeamLeaderId(GUEST_ID), new RoomPlayerId(1));
        return room;
    }
}
