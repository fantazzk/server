package com.naminhyeok.fantazzk.architecture;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;

class RoomTestPackageStructureTest {
    @Test
    void room_unit_test는_역할별_테스트_패키지에_위치한다() {
        assertClassMissing("com.naminhyeok.fantazzk.room.RoomAggregateTest");
        assertClassMissing("com.naminhyeok.fantazzk.room.CreateRoomTest");
        assertClassMissing("com.naminhyeok.fantazzk.room.RoomQueryApiControllerWebMvcTest");
        assertClassMissing("com.naminhyeok.fantazzk.room.RoomAuctionDeadlineSchedulerTest");
        assertClassMissing("com.naminhyeok.fantazzk.room.SupabaseRoomRealtimePublisherTest");
        assertClassMissing("com.naminhyeok.fantazzk.room.RoomApiTestFixtures");

        assertClassPresent("com.naminhyeok.fantazzk.room.domain.RoomAggregateTest");
        assertClassPresent("com.naminhyeok.fantazzk.room.application.CreateRoomTest");
        assertClassPresent("com.naminhyeok.fantazzk.room.web.RoomQueryApiControllerWebMvcTest");
        assertClassPresent("com.naminhyeok.fantazzk.room.infrastructure.schedule.RoomAuctionDeadlineSchedulerTest");
        assertClassPresent("com.naminhyeok.fantazzk.room.infrastructure.realtime.SupabaseRoomRealtimePublisherTest");
        assertClassPresent("com.naminhyeok.fantazzk.room.support.RoomApiTestFixtures");
    }

    private void assertClassMissing(String className) {
        assertThatThrownBy(() -> Class.forName(className))
            .isInstanceOf(ClassNotFoundException.class);
    }

    private void assertClassPresent(String className) {
        assertThatCode(() -> Class.forName(className)).doesNotThrowAnyException();
    }
}
