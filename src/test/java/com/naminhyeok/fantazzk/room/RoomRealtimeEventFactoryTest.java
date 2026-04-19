package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RoomRealtimeEventFactoryTest {
    private static final Instant CREATED_AT = Instant.parse("2026-04-09T00:00:00Z");
    private static final Instant PUBLISHED_AT = Instant.parse("2026-04-09T00:10:00Z");

    @Test
    void room_membership_updated는_참여자_변경에_필요한_payload를_담는다() {
        Room room = waitingAuctionRoom();

        RoomRealtimeEvent event = RoomRealtimeEventFactory.roomMembershipUpdated(room, PUBLISHED_AT);

        assertThat(event).isInstanceOf(RoomMembershipUpdatedEvent.class);
        RoomMembershipUpdatedEvent membershipUpdated = (RoomMembershipUpdatedEvent) event;
        assertThat(membershipUpdated.eventType()).isEqualTo("ROOM_MEMBERSHIP_UPDATED");
        assertThat(membershipUpdated.roomCode()).isEqualTo(room.getCode());
        assertThat(membershipUpdated.membership().teamCount()).isEqualTo(2);
        assertThat(membershipUpdated.membership().joinedLeaderCount()).isEqualTo(1);
        assertThat(membershipUpdated.membership().leaders()).hasSize(1);
    }

    @Test
    void game_started는_게임_진입에_필요한_식별자만_담는다() {
        Room room = startedAuctionRoom();
        Game game = startedGameOf(room);

        RoomRealtimeEvent event = RoomRealtimeEventFactory.gameStarted(new StartedRoomSnapshot(room, game), PUBLISHED_AT);

        assertThat(event).isInstanceOf(GameStartedEvent.class);
        GameStartedEvent gameStarted = (GameStartedEvent) event;
        assertThat(gameStarted.eventType()).isEqualTo("GAME_STARTED");
        assertThat(gameStarted.roomCode()).isEqualTo(room.getCode());
        assertThat(gameStarted.gameId()).isEqualTo(game.getId().gameId().toString());
        assertThat(gameStarted.gameStart().mode()).isEqualTo("AUCTION");
        assertThat(gameStarted.gameStart().status()).isEqualTo("IN_PROGRESS");
    }

    @Test
    void game_auction_progress_updated_version은_room과_game_version을_합산한다() throws Exception {
        Room room = startedAuctionRoom();
        Game game = startedGameOf(room);
        setVersion(Room.class, room, 1L);
        setVersion(Game.class, game, 7L);

        RoomRealtimeEvent event = RoomRealtimeEventFactory.gameAuctionProgressUpdated(new StartedRoomSnapshot(room, game), PUBLISHED_AT);

        assertThat(event.snapshotVersion()).isEqualTo(8L);
    }

    private Room waitingAuctionRoom() {
        return Room.createFromTemplate(
            "AUC002",
            new TeamLeaderId("host-1"),
            "호스트",
            "host-action-token",
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
            CREATED_AT
        );
    }

    private Room startedAuctionRoom() {
        Room room = waitingAuctionRoom();
        room.join(new TeamLeaderId("guest-1"), "게스트1", "guest-action-token");
        room.start(new TeamLeaderId("host-1"), CREATED_AT);
        return room;
    }

    private Game startedGameOf(Room room) {
        StartedGameSnapshot snapshot = new StartedGameSnapshot(
            room.getId(),
            room.getCode(),
            room.getStartedGameId(),
            room.getStartedAt(),
            room.getMode(),
            GameRules.auction(room.getTeamCount(), room.getTeamSize(), room.getBudget(), room.getPickBanTime(), room.getMinBidUnit(), room.getPositionLimit()),
            List.of(
                GameParticipant.auction(new TeamLeaderId("host-1"), "호스트", 300),
                GameParticipant.auction(new TeamLeaderId("guest-1"), "게스트1", 300)
            ),
            List.of(
                new GamePlayer(new RoomPlayerId(0), "선수1", "TOP", 0),
                new GamePlayer(new RoomPlayerId(1), "선수2", "JUNGLE", 1)
            )
        );
        return new GameFactory().create(snapshot);
    }

    private static void setVersion(Class<?> owner, Object target, long version) throws Exception {
        var field = owner.getDeclaredField("version");
        field.setAccessible(true);
        field.setLong(target, version);
    }
}
