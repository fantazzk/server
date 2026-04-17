package com.naminhyeok.fantazzk.auction;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Test;

class AuctionModuleStructureTest {
    @Test
    void 공개_계약은_애플리케이션_서비스와_상태_표현만_노출한다() throws Exception {
        assertThat(isPublic("com.naminhyeok.fantazzk.auction.AuctionRoomLifecycle")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.auction.AuctionRoomPlay")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.auction.AuctionRoomStateReader")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.auction.AuctionRoomState")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.auction.AuctionSettlement")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.auction.AuctionBid")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.auction.AuctionTarget")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.auction.AuctionRoomSetup")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.auction.AuctionPlayerSeed")).isTrue();
    }

    @Test
    void 내부_도메인_구성은_package_private이다() throws Exception {
        assertThat(isPublic("com.naminhyeok.fantazzk.auction.AuctionRoomId")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.auction.AuctionRooms")).isFalse();
        assertThat(isPublic("com.naminhyeok.fantazzk.auction.AuctionRoom")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.auction.AuctionPlayer")).isFalse();
        assertThat(isPublic("com.naminhyeok.fantazzk.auction.AuctionTeamLeader")).isFalse();
        assertThat(isPublic("com.naminhyeok.fantazzk.auction.AuctionTeamMember")).isFalse();
        assertThat(isPublic("com.naminhyeok.fantazzk.auction.AuctionRoomException")).isFalse();
    }

    private boolean isPublic(String className) throws Exception {
        return Modifier.isPublic(Class.forName(className).getModifiers());
    }
}
