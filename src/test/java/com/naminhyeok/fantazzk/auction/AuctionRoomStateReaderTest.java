package com.naminhyeok.fantazzk.auction;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AuctionRoomStateReaderTest {
    @Test
    void 상태를_스냅샷으로_읽는다() {
        AuctionRoomStateReader reader = new AuctionRoomStateReader(AuctionTestFixtures.inMemoryRooms(AuctionTestFixtures.startedRoom()));

        AuctionRoomState state = reader.read("ROOM01");

        assertThat(state.currentRound()).isEqualTo(1);
        assertThat(state.currentTarget().playerId()).isEqualTo(0);
    }
}
