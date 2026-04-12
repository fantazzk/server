package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class RoomRealtimePublishingTest {
    private static final Instant CREATED_AT = Instant.parse("2024-01-01T10:00:00Z");
    private static final Instant PUBLISHED_AT = Instant.parse("2024-01-01T10:00:30Z");
    private static final String HOST_ID = "leader-host";
    private static final String HOST_ACTION_TOKEN = "host-token";
    private static final String GUEST_ID = "leader-guest";
    private static final String GUEST_ACTION_TOKEN = "guest-token";

    @Test
    void join은_저장된_room_스냅샷을_publish한다() {
        Room room = waitingAuctionRoom();
        InMemoryRooms rooms = new InMemoryRooms(room);
        RecordingRoomRealtimePublisher roomRealtimePublisher = new RecordingRoomRealtimePublisher();
        JoinRoom joinRoom =
            new JoinRoom(
                rooms,
                fixedLeaderIdentityIssuer(),
                roomRealtimePublisher
            );

        RoomTeamLeader joined = joinRoom.join(room.getCode(), "게스트");

        assertThat(joined.getId().value()).isEqualTo(GUEST_ID);
        assertThat(roomRealtimePublisher.events).hasSize(1);
        RoomRealtimeSnapshotEvent event = roomRealtimePublisher.events.getFirst();
        assertThat(event.roomCode()).isEqualTo(room.getCode());
        assertThat(event.publishedAt()).isEqualTo(PUBLISHED_AT);
        assertThat(event.room().teamLeaders()).hasSize(2);
        assertThat(event.room().teamLeaders().get(1).nickname()).isEqualTo("게스트");
    }

    @Test
    void settleIfDue는_기한이_지나면_정산된_latest_room_스냅샷을_publish한다() {
        Room room = startedAuctionRoom();
        InMemoryRooms rooms = new InMemoryRooms(room);
        RecordingRoomRealtimePublisher roomRealtimePublisher = new RecordingRoomRealtimePublisher();
        SettleAuctionAttempt settleAuctionAttempt =
            new SettleAuctionAttempt(rooms, Clock.fixed(PUBLISHED_AT, ZoneOffset.UTC), roomRealtimePublisher);

        Room settled = settleAuctionAttempt.settleIfDue(room.getCode());

        assertThat(settled.getCurrentAuctionRound()).isEqualTo(2);
        assertThat(roomRealtimePublisher.events).hasSize(1);
        RoomRealtimeSnapshotEvent event = roomRealtimePublisher.events.getFirst();
        assertThat(event.roomCode()).isEqualTo(room.getCode());
        assertThat(event.publishedAt()).isEqualTo(PUBLISHED_AT);
        assertThat(event.room().progress().currentRound()).isEqualTo(2);
        assertThat(event.room().progress().currentAuctionRoundEndsAt()).isEqualTo(PUBLISHED_AT.plusSeconds(30));
    }

    private static TeamLeaderIdentityIssuer fixedLeaderIdentityIssuer() {
        return () -> new TeamLeaderIdentityIssuer.TeamLeaderIdentity(GUEST_ID, GUEST_ACTION_TOKEN);
    }

    private static Room waitingAuctionRoom() {
        return Room.createFromTemplate(
            "ROOM01",
            new TeamLeaderId(HOST_ID),
            "호스트",
            HOST_ACTION_TOKEN,
            new RoomTemplateSpec(
                RoomTemplateSpec.Mode.AUCTION,
                2,
                2,
                300,
                30,
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

    private static Room startedAuctionRoom() {
        Room room = waitingAuctionRoom();
        room.join(new TeamLeaderId(GUEST_ID), "게스트", GUEST_ACTION_TOKEN);
        room.start(new TeamLeaderId(HOST_ID), CREATED_AT);
        return room;
    }

    private static final class RecordingRoomRealtimePublisher implements RoomRealtimePublisher {
        private final List<RoomRealtimeSnapshotEvent> events = new ArrayList<>();

        @Override
        public void publishAfterCommit(Room room) {
            events.add(RoomRealtimeSnapshotEvent.from(room, PUBLISHED_AT));
        }
    }

    private static final class InMemoryRooms implements Rooms {
        private Room room;

        private InMemoryRooms(Room room) {
            this.room = room;
        }

        @Override
        public Room save(Room room) {
            this.room = room;
            return room;
        }

        @Override
        public Room saveAndFlush(Room room) {
            this.room = room;
            return room;
        }

        @Override
        public java.util.Optional<Room> findById(RoomId id) {
            return java.util.Optional.ofNullable(room).filter(it -> it.getId().equals(id));
        }

        @Override
        public java.util.Optional<Room> findByCode(String code) {
            return java.util.Optional.ofNullable(room).filter(it -> it.getCode().equals(code));
        }
    }
}
