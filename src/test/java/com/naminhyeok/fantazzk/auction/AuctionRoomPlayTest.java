package com.naminhyeok.fantazzk.auction;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AuctionRoomPlayTest {
    @Test
    void 입찰하면_저장하고_입찰_정보를_돌려준다() {
        RecordingAuctionRooms rooms = new RecordingAuctionRooms(AuctionTestFixtures.startedRoom());
        AuctionRoomPlay play = new AuctionRoomPlay(rooms);

        AuctionBid bid = play.placeBid("ROOM01", AuctionTestFixtures.HOST_ID, 100, AuctionTestFixtures.CREATED_AT.plusSeconds(1));

        assertThat(bid.amount()).isEqualTo(100);
        assertThat(rooms.saveAndFlushCalled).isTrue();
    }

    private static final class RecordingAuctionRooms implements AuctionRooms {
        private AuctionRoom savedRoom;
        private boolean saveAndFlushCalled;

        private RecordingAuctionRooms(AuctionRoom room) {
            this.savedRoom = room;
        }

        @Override
        public AuctionRoom save(AuctionRoom room) {
            this.savedRoom = room;
            return room;
        }

        @Override
        public AuctionRoom saveAndFlush(AuctionRoom room) {
            this.savedRoom = room;
            this.saveAndFlushCalled = true;
            return room;
        }

        @Override
        public java.util.Optional<AuctionRoom> findByCode(String code) {
            return java.util.Optional.ofNullable(savedRoom).filter(room -> room.readState().code().equals(code));
        }

        @Override
        public java.util.List<AuctionRoom> findInProgressAuctionRooms() {
            return java.util.List.of();
        }
    }
}
