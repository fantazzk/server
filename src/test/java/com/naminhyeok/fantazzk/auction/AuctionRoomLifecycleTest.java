package com.naminhyeok.fantazzk.auction;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AuctionRoomLifecycleTest {
    @Test
    void 생성하면_저장하고_초기_상태를_돌려준다() {
        RecordingAuctionRooms rooms = new RecordingAuctionRooms();
        AuctionRoomLifecycle lifecycle = new AuctionRoomLifecycle(rooms);

        AuctionRoomState state = lifecycle.create(
            "ROOM01",
            AuctionTestFixtures.HOST_ID,
            AuctionTestFixtures.HOST_NICKNAME,
            AuctionTestFixtures.CREATED_AT,
            AuctionTestFixtures.setup()
        );

        assertThat(rooms.savedRoom).isNotNull();
        assertThat(state.code()).isEqualTo("ROOM01");
        assertThat(state.status()).isEqualTo(AuctionRoomStatus.WAITING);
        assertThat(state.currentRound()).isNull();
    }

    @Test
    void 팀장을_추가하면_saveAndFlush를_호출한다() {
        RecordingAuctionRooms rooms = new RecordingAuctionRooms(AuctionTestFixtures.waitingRoom());
        AuctionRoomLifecycle lifecycle = new AuctionRoomLifecycle(rooms);

        AuctionRoomState state = lifecycle.addLeader("ROOM01", AuctionTestFixtures.GUEST_ID, AuctionTestFixtures.GUEST_NICKNAME);

        assertThat(rooms.saveAndFlushCalled).isTrue();
        assertThat(state.status()).isEqualTo(AuctionRoomStatus.WAITING);
    }

    @Test
    void 시작하면_라운드가_초기화된다() {
        RecordingAuctionRooms rooms = new RecordingAuctionRooms(AuctionTestFixtures.waitingRoomWithGuestLeader());
        AuctionRoomLifecycle lifecycle = new AuctionRoomLifecycle(rooms);

        AuctionRoomState state = lifecycle.start("ROOM01", AuctionTestFixtures.HOST_ID, AuctionTestFixtures.CREATED_AT);

        assertThat(state.currentRound()).isEqualTo(1);
        assertThat(state.currentRoundEndsAt()).isEqualTo(AuctionTestFixtures.CREATED_AT.plusSeconds(AuctionTestFixtures.PICK_BAN_TIME));
    }

    @Test
    void 정산하면_정산결과를_돌려준다() {
        RecordingAuctionRooms rooms = new RecordingAuctionRooms(AuctionTestFixtures.startedRoomWithWinningBid());
        AuctionRoomLifecycle lifecycle = new AuctionRoomLifecycle(rooms);

        AuctionSettlement settlement = lifecycle.settle("ROOM01", AuctionTestFixtures.CREATED_AT.plusSeconds(AuctionTestFixtures.PICK_BAN_TIME + 2L));

        assertThat(settlement.outcome()).isEqualTo(AuctionOutcome.SOLD);
        assertThat(rooms.saveAndFlushCalled).isTrue();
    }

    private static final class RecordingAuctionRooms implements AuctionRooms {
        private AuctionRoom savedRoom;
        private boolean saveAndFlushCalled;

        private RecordingAuctionRooms() {
        }

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
