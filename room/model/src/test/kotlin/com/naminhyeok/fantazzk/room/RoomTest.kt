package com.naminhyeok.fantazzk.room

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class RoomTest {
    @Nested
    inner class `모드별 생성` {
        @Test
        fun `경매 방은 예산과 함께 생성된다`() {
            val room = room(mode = TeamBuildingMode.AUCTION, budget = 300)

            assertThat(room.isAuction()).isTrue()
            assertThat(room.isDraft()).isFalse()
            assertThat(room.budget).isEqualTo(300)
        }

        @Test
        fun `드래프트 방은 순서 전략과 함께 생성된다`() {
            val room = room(mode = TeamBuildingMode.DRAFT, draftOrderStrategy = DraftOrderStrategy.SNAKE)

            assertThat(room.isDraft()).isTrue()
            assertThat(room.isAuction()).isFalse()
            assertThat(room.draftOrderStrategy).isEqualTo(DraftOrderStrategy.SNAKE)
        }
    }

    @Nested
    inner class `상태 판별` {
        @ParameterizedTest(name = "{0} 상태에서 isWaiting={1}")
        @EnumSource(RoomStatus::class)
        fun `isWaiting은 WAITING 상태에서만 true를 반환한다`(status: RoomStatus) {
            val room = room(status = status)
            assertThat(room.isWaiting()).isEqualTo(status == RoomStatus.WAITING)
        }

        @ParameterizedTest(name = "{0} 상태에서 isInProgress={1}")
        @EnumSource(RoomStatus::class)
        fun `isInProgress는 IN_PROGRESS 상태에서만 true를 반환한다`(status: RoomStatus) {
            val room = room(status = status)
            assertThat(room.isInProgress()).isEqualTo(status == RoomStatus.IN_PROGRESS)
        }
    }

    @Nested
    inner class `파생 값` {
        @Test
        fun `picksPerTeam은 teamSize에서 1을 뺀 값이다`() {
            assertThat(room(teamSize = 3).picksPerTeam).isEqualTo(2)
            assertThat(room(teamSize = 5).picksPerTeam).isEqualTo(4)
            assertThat(room(teamSize = 1).picksPerTeam).isEqualTo(0)
        }
    }

    @Nested
    inner class Identity {
        @Test
        fun `RoomIdentity를 생성할 수 있다`() {
            val identity = RoomIdentity.of(42L)
            assertThat(identity.roomId).isEqualTo(42L)
        }
    }

    private fun room(
        status: RoomStatus = RoomStatus.WAITING,
        mode: TeamBuildingMode = TeamBuildingMode.AUCTION,
        teamSize: Int = 3,
        budget: Int? = null,
        draftOrderStrategy: DraftOrderStrategy? = null,
    ) = Room(
        code = "TEST01",
        hostId = "host",
        status = status,
        mode = mode,
        teamCount = 2,
        teamSize = teamSize,
        budget = budget,
        draftOrderStrategy = draftOrderStrategy,
    )
}
