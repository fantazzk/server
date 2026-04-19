package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TeamLeaderSessionResponseTest {
    @Test
    void role_필드는_enum_타입이다() {
        Class<?> roleType = TeamLeaderSessionResponse.class.getRecordComponents()[1].getType();

        assertThat(roleType.getName()).isEqualTo("com.naminhyeok.fantazzk.room.TeamLeaderRole");
        assertThat(roleType.isEnum()).isTrue();
    }

    @Test
    void 호스트_세션은_enum_HOST_역할을_반환한다() throws Exception {
        Room room = RoomApiTestFixtures.waitingAuctionRoom();
        RoomTeamLeader host = room.getLeaders().getFirst();

        TeamLeaderSessionResponse response = TeamLeaderSessionResponse.from(room, host);
        Object role = TeamLeaderSessionResponse.class.getMethod("role").invoke(response);

        assertThat(role.toString()).isEqualTo("HOST");
        assertThat(role.getClass().isEnum()).isTrue();
    }
}
