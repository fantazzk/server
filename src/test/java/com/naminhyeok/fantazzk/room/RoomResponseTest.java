package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RoomResponseTest {
    private static final Instant CREATED_AT = Instant.parse("2026-04-09T00:00:00Z");
    private static final String HOST_ID = "host-1";
    private static final String HOST_ACTION_TOKEN = "host-action-token";
    private static final String GUEST_ONE_ID = "guest-1";
    private static final String GUEST_ONE_ACTION_TOKEN = "guest-1-action-token";
    private static final String GUEST_TWO_ID = "guest-2";
    private static final String GUEST_TWO_ACTION_TOKEN = "guest-2-action-token";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

    @Test
    void 드래프트_방_응답은_자리_기준_예상_시작_순서를_슬롯으로_반환한다() {
        Room room = threeTeamDraftRoom();
        room.join(new TeamLeaderId(GUEST_ONE_ID), "게스트1", GUEST_ONE_ACTION_TOKEN);
        room.join(new TeamLeaderId(GUEST_TWO_ID), "게스트2", GUEST_TWO_ACTION_TOKEN);
        room.selectDraftPosition(new TeamLeaderId(HOST_ID), 2);
        room.selectDraftPosition(new TeamLeaderId(GUEST_ONE_ID), 1);
        room.selectDraftPosition(new TeamLeaderId(GUEST_TWO_ID), 3);

        RoomResponse response = RoomResponse.from(RoomDetails.from(room));

        assertThat(response.draftOrderPreview()).isNotNull();
        assertThat(response.draftOrderPreview().slots())
            .extracting(DraftOrderSlotResponse::draftPosition, DraftOrderSlotResponse::leaderId, DraftOrderSlotResponse::nickname)
            .containsExactly(
                tuple(1, GUEST_ONE_ID, "게스트1"),
                tuple(2, HOST_ID, "호스트"),
                tuple(3, GUEST_TWO_ID, "게스트2")
            );
    }

    @Test
    void 드래프트_방_응답은_비어있는_자리를_null_슬롯으로_반환한다() {
        Room room = threeTeamDraftRoom();
        room.join(new TeamLeaderId(GUEST_ONE_ID), "게스트1", GUEST_ONE_ACTION_TOKEN);
        room.selectDraftPosition(new TeamLeaderId(HOST_ID), 2);

        RoomResponse response = RoomResponse.from(RoomDetails.from(room));

        assertThat(response.draftOrderPreview()).isNotNull();
        assertThat(response.draftOrderPreview().slots())
            .extracting(DraftOrderSlotResponse::draftPosition, DraftOrderSlotResponse::leaderId, DraftOrderSlotResponse::nickname)
            .containsExactly(
                tuple(1, null, null),
                tuple(2, HOST_ID, "호스트"),
                tuple(3, null, null)
            );
    }

    @Test
    void 경매_방_응답은_드래프트_preview를_포함하지_않는다() {
        Room room =
            Room.createFromTemplate(
                "AUC001",
                new TeamLeaderId(HOST_ID),
                "호스트",
                HOST_ACTION_TOKEN,
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
                CREATED_AT
            );

        RoomResponse response = RoomResponse.from(RoomDetails.from(room));

        assertThat(response.draftOrderPreview()).isNull();
    }

    @Test
    void 시작된_방_응답은_game이_없으면_room_라이브_진행정보를_투영하지_않는다() {
        Room room = startedAuctionRoom();

        RoomResponse response = RoomResponse.from(RoomDetails.from(room));

        assertThat(response.status()).isEqualTo("STARTED");
        assertThat(response.members()).isEmpty();
        assertThat(response.progress().currentRound()).isNull();
        assertThat(response.progress().currentAuctionRoundEndsAt()).isNull();
        assertThat(response.progress().currentAuctionTarget()).isNull();
        assertThat(response.progress().highestBidAmount()).isNull();
        assertThat(response.progress().leadingLeaderId()).isNull();
        assertThat(response.progress().bidCount()).isNull();
    }

    @Test
    void 시작된_경매_방_응답은_Game의_라이브_필드를_우선한다() {
        Room room = startedAuctionRoom();
        AuctionGame game = (AuctionGame) startedGame(room);
        game.placeBid(new TeamLeaderId(HOST_ID), 100, CREATED_AT.plusSeconds(1));
        game.settleAuction(CREATED_AT.plusSeconds(46));

        RoomResponse response = RoomResponse.from(new RoomDetails(room, game));

        assertThat(response.status()).isEqualTo("IN_PROGRESS");
        assertThat(response.teamLeaders())
            .extracting(TeamLeaderResponse::id, TeamLeaderResponse::remainingBudget)
            .containsExactly(
                tuple(HOST_ID, 200),
                tuple(GUEST_ONE_ID, 300)
            );
        assertThat(response.players())
            .extracting(RoomPlayerResponse::name, RoomPlayerResponse::status)
            .containsExactly(
                tuple("선수1", "ASSIGNED"),
                tuple("선수2", "AVAILABLE")
            );
        assertThat(response.members())
            .extracting(RoomMemberResponse::teamLeaderId, RoomMemberResponse::playerName)
            .containsExactly(tuple(HOST_ID, "선수1"));
        assertThat(response.progress().currentRound()).isEqualTo(2);
        assertThat(response.progress().currentAuctionTarget().name()).isEqualTo("선수2");
        assertThat(response.progress().highestBidAmount()).isNull();
    }

    @Test
    void 완료된_경매_방_응답은_완료_상태와_빈_progress를_반환한다() {
        Room room = startedAuctionRoom();
        AuctionGame game = (AuctionGame) startedGame(room);
        game.placeBid(new TeamLeaderId(HOST_ID), 100, CREATED_AT.plusSeconds(1));
        game.settleAuction(CREATED_AT.plusSeconds(46));
        game.placeBid(new TeamLeaderId(GUEST_ONE_ID), 110, CREATED_AT.plusSeconds(47));
        game.settleAuction(CREATED_AT.plusSeconds(92));

        RoomResponse response = RoomResponse.from(new RoomDetails(room, game));

        assertThat(response.status()).isEqualTo("COMPLETED");
        assertThat(response.progress().currentRound()).isNull();
        assertThat(response.progress().currentAuctionRoundEndsAt()).isNull();
        assertThat(response.progress().currentAuctionTarget()).isNull();
        assertThat(response.progress().highestBidAmount()).isNull();
        assertThat(response.progress().leadingLeaderId()).isNull();
        assertThat(response.progress().bidCount()).isNull();
    }

    @Test
    void 시작된_드래프트_방_응답은_DraftGame의_라이브_필드를_우선한다() {
        Room room = startedDraftRoom();
        DraftGame game = (DraftGame) startedGame(room, RoomTemplateSpec.Mode.DRAFT);
        game.pick(new TeamLeaderId(HOST_ID), "선수1");

        RoomResponse response = RoomResponse.from(new RoomDetails(room, game));

        assertThat(response.status()).isEqualTo("IN_PROGRESS");
        assertThat(response.players())
            .extracting(RoomPlayerResponse::name, RoomPlayerResponse::status)
            .containsExactly(
                tuple("선수1", "ASSIGNED"),
                tuple("선수2", "AVAILABLE")
            );
        assertThat(response.members())
            .extracting(RoomMemberResponse::teamLeaderId, RoomMemberResponse::playerName)
            .containsExactly(tuple(HOST_ID, "선수1"));
        assertThat(response.progress().currentTurnIndex()).isEqualTo(1);
        assertThat(response.progress().currentRound()).isEqualTo(1);
        assertThat(response.progress().currentLeaderId()).isEqualTo(GUEST_ONE_ID);
        assertThat(response.progress().currentRoundLeaderIds()).containsExactly(HOST_ID, GUEST_ONE_ID);
    }

    @Test
    void 방_응답은_Game_규칙값보다_Room_규칙값을_우선한다() {
        Room room = startedAuctionRoom();
        Game game = mismatchedGame(room);

        RoomResponse response = RoomResponse.from(new RoomDetails(room, game));

        assertThat(response.teamCount()).isEqualTo(2);
        assertThat(response.teamSize()).isEqualTo(2);
        assertThat(response.budget()).isEqualTo(300);
        assertThat(response.minBidUnit()).isEqualTo(10);
        assertThat(response.draftOrderStrategy()).isNull();
    }

    private Room threeTeamDraftRoom() {
        return Room.createFromTemplate(
            "DRF001",
            new TeamLeaderId(HOST_ID),
            "호스트",
            HOST_ACTION_TOKEN,
            new RoomTemplateSpec(
                RoomTemplateSpec.Mode.DRAFT,
                3,
                2,
                null,
                30,
                null,
                RoomTemplateSpec.DraftOrderStrategy.SNAKE,
                List.of(
                    new RoomTemplateSpec.Player(new RoomPlayerId(0), "선수1", "TOP", 0),
                    new RoomTemplateSpec.Player(new RoomPlayerId(1), "선수2", "JUNGLE", 1),
                    new RoomTemplateSpec.Player(new RoomPlayerId(2), "선수3", "MID", 2)
                )
            ),
            CREATED_AT
        );
    }

    private Room startedAuctionRoom() {
        Room room =
            Room.createFromTemplate(
                "AUC002",
                new TeamLeaderId(HOST_ID),
                "호스트",
                HOST_ACTION_TOKEN,
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
                CREATED_AT
            );
        room.join(new TeamLeaderId(GUEST_ONE_ID), "게스트1", GUEST_ONE_ACTION_TOKEN);
        room.start(new TeamLeaderId(HOST_ID), CREATED_AT);
        return room;
    }

    private Room startedDraftRoom() {
        Room room =
            Room.createFromTemplate(
                "DRF002",
                new TeamLeaderId(HOST_ID),
                "호스트",
                HOST_ACTION_TOKEN,
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
                CREATED_AT
            );
        room.join(new TeamLeaderId(GUEST_ONE_ID), "게스트1", GUEST_ONE_ACTION_TOKEN);
        room.selectDraftPosition(new TeamLeaderId(HOST_ID), 1);
        room.selectDraftPosition(new TeamLeaderId(GUEST_ONE_ID), 2);
        room.start(new TeamLeaderId(HOST_ID), CREATED_AT);
        return room;
    }

    private Game startedGame(Room room, RoomTemplateSpec.Mode mode) {
        Room startableRoom =
            Room.createFromTemplate(
                room.getCode(),
                new TeamLeaderId(HOST_ID),
                "호스트",
                HOST_ACTION_TOKEN,
                new RoomTemplateSpec(
                    mode,
                    2,
                    2,
                    mode == RoomTemplateSpec.Mode.AUCTION ? 300 : null,
                    mode == RoomTemplateSpec.Mode.AUCTION ? 15 : 30,
                    mode == RoomTemplateSpec.Mode.AUCTION ? 10 : null,
                    mode == RoomTemplateSpec.Mode.AUCTION ? null : RoomTemplateSpec.DraftOrderStrategy.SNAKE,
                    List.of(
                        new RoomTemplateSpec.Player(new RoomPlayerId(0), "선수1", "TOP", 0),
                        new RoomTemplateSpec.Player(new RoomPlayerId(1), "선수2", "JUNGLE", 1)
                    )
                ),
                CREATED_AT
            );
        startableRoom.join(new TeamLeaderId(GUEST_ONE_ID), "게스트1", GUEST_ONE_ACTION_TOKEN);
        if (mode == RoomTemplateSpec.Mode.DRAFT) {
            startableRoom.selectDraftPosition(new TeamLeaderId(HOST_ID), 1);
            startableRoom.selectDraftPosition(new TeamLeaderId(GUEST_ONE_ID), 2);
        }
        StartedGameSnapshot snapshot = startableRoom.start(
            new TeamLeaderId(HOST_ID),
            new GameId(UUID.fromString("00000000-0000-0000-0000-000000000101")),
            CREATED_AT
        );
        return new GameFactory().create(snapshot);
    }

    private Game startedGame(Room room) {
        return startedGame(room, RoomTemplateSpec.Mode.AUCTION);
    }

    private Game mismatchedGame(Room room) {
        return new AuctionGame(
            new GameId(UUID.fromString("00000000-0000-0000-0000-000000000202")),
            room.getId(),
            room.getCode(),
            CREATED_AT,
            new GameRules(4, 4, 999, 99, 99, 3, RoomTemplateSpec.DraftOrderStrategy.SNAKE),
            List.of(
                new GameParticipant(new TeamLeaderId(HOST_ID), "호스트", null, 999),
                new GameParticipant(new TeamLeaderId(GUEST_ONE_ID), "게스트1", null, 999)
            ),
            List.of(
                new GamePlayer(new RoomPlayerId(0), "다른선수1", "TOP", 0),
                new GamePlayer(new RoomPlayerId(1), "다른선수2", "MID", 1)
            ),
            1,
            CREATED_AT.plusSeconds(99)
        );
    }
}
