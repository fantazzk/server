package com.naminhyeok.fantazzk.auction;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class AuctionTestFixtures {
    static final Instant CREATED_AT = Instant.parse("2026-04-09T00:00:00Z");
    static final int PICK_BAN_TIME = 45;
    static final int MIN_BID_UNIT = 10;
    static final String HOST_ID = "host-1";
    static final String HOST_NICKNAME = "호스트";
    static final String GUEST_ID = "guest-1";
    static final String GUEST_NICKNAME = "게스트";

    private AuctionTestFixtures() {
    }

    static AuctionRoomSetup setup() {
        return new AuctionRoomSetup(
            2,
            2,
            300,
            PICK_BAN_TIME,
            MIN_BID_UNIT,
            null,
            List.of(
                new AuctionPlayerSeed(0, "선수1", "TOP", 0),
                new AuctionPlayerSeed(1, "선수2", "JUNGLE", 1)
            )
        );
    }

    static AuctionRoom waitingRoom() {
        return AuctionRoom.create("ROOM01", HOST_ID, HOST_NICKNAME, CREATED_AT, setup());
    }

    static AuctionRoom waitingRoomWithGuestLeader() {
        AuctionRoom room = waitingRoom();
        room.addLeader(GUEST_ID, GUEST_NICKNAME);
        return room;
    }

    static AuctionRoom startedRoom() {
        AuctionRoom room = waitingRoomWithGuestLeader();
        room.start(HOST_ID, CREATED_AT);
        return room;
    }

    static AuctionRoom startedRoomWithoutBids() {
        return startedRoom();
    }

    static AuctionRoom startedRoomWithWinningBid() {
        AuctionRoom room = startedRoom();
        room.placeBid(HOST_ID, 100, CREATED_AT.plusSeconds(1));
        room.placeBid(GUEST_ID, 150, CREATED_AT.plusSeconds(2));
        return room;
    }

    static AuctionRoom startedRoomWithPassedRound() {
        AuctionRoom room = startedRoom();
        room.settle(CREATED_AT.plusSeconds(PICK_BAN_TIME));
        return room;
    }

    static AuctionRooms inMemoryRooms(AuctionRoom room) {
        return new InMemoryAuctionRooms(room);
    }

    static AuctionRooms emptyRooms() {
        return new InMemoryAuctionRooms();
    }

    static AuctionRoomSetup setupWithThreePlayers() {
        return new AuctionRoomSetup(
            2,
            2,
            300,
            PICK_BAN_TIME,
            MIN_BID_UNIT,
            null,
            List.of(
                new AuctionPlayerSeed(0, "선수1", "TOP", 0),
                new AuctionPlayerSeed(1, "선수2", "JUNGLE", 1),
                new AuctionPlayerSeed(2, "선수3", "MID", 2)
            )
        );
    }

    private static final class InMemoryAuctionRooms implements AuctionRooms {
        private final Map<String, AuctionRoom> rooms = new LinkedHashMap<>();

        private InMemoryAuctionRooms() {
        }

        private InMemoryAuctionRooms(AuctionRoom room) {
            rooms.put(room.readState().code(), room);
        }

        @Override
        public AuctionRoom save(AuctionRoom room) {
            rooms.put(room.readState().code(), room);
            return room;
        }

        @Override
        public AuctionRoom saveAndFlush(AuctionRoom room) {
            rooms.put(room.readState().code(), room);
            return room;
        }

        @Override
        public Optional<AuctionRoom> findByCode(String code) {
            return Optional.ofNullable(rooms.get(code));
        }

        @Override
        public List<AuctionRoom> findInProgressAuctionRooms() {
            return rooms.values().stream()
                .filter(room -> room.readState().status() == AuctionRoomStatus.IN_PROGRESS)
                .toList();
        }
    }
}
