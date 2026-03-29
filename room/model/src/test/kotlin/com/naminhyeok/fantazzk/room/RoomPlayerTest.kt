package com.naminhyeok.fantazzk.room

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.time.Instant

class RoomPlayerTest {
    @Nested
    inner class `생성 계약` {
        @Test
        fun `새 선수는 기본 식별자와 상태를 가진다`() {
            val beforeCreate = Instant.now()
            val player = RoomPlayer(roomId = 1L, name = "선수1", displayOrder = 0)
            val afterCreate = Instant.now()

            assertThat(player.roomPlayerId).isZero()
            assertThat(player.roomId).isEqualTo(1L)
            assertThat(player.name).isEqualTo("선수1")
            assertThat(player.status).isEqualTo(PlayerStatus.AVAILABLE)
            assertThat(player.displayOrder).isZero()
            assertThat(player.createdAt).isBetween(beforeCreate, afterCreate)
            assertThat(player.updatedAt).isBetween(beforeCreate, afterCreate)
        }
    }

    @Nested
    inner class `상태 판별` {
        @ParameterizedTest(name = "{0} 상태의 선수 isAvailable 검증")
        @EnumSource(PlayerStatus::class)
        fun `isAvailable은 AVAILABLE 상태에서만 true를 반환한다`(status: PlayerStatus) {
            val player = RoomPlayer(roomId = 1L, name = "선수1", status = status, displayOrder = 0)
            assertThat(player.isAvailable()).isEqualTo(status == PlayerStatus.AVAILABLE)
        }
    }

    @Nested
    inner class `모델 변환` {
        @Test
        fun `RoomPlayerModel에서 RoomPlayer를 복원할 수 있다`() {
            val createdAt = Instant.parse("2025-01-01T00:00:00Z")
            val updatedAt = Instant.parse("2025-01-02T00:00:00Z")
            val model =
                playerModel(
                    roomPlayerId = 9L,
                    roomId = 3L,
                    name = "선수9",
                    status = PlayerStatus.ASSIGNED,
                    displayOrder = 7,
                    createdAt = createdAt,
                    updatedAt = updatedAt,
                )

            val player = RoomPlayer.from(model)

            assertThat(player).isEqualTo(
                RoomPlayer(
                    roomPlayerId = 9L,
                    roomId = 3L,
                    name = "선수9",
                    status = PlayerStatus.ASSIGNED,
                    displayOrder = 7,
                    createdAt = createdAt,
                    updatedAt = updatedAt,
                ),
            )
        }
    }

    private fun playerModel(
        roomPlayerId: Long,
        roomId: Long,
        name: String,
        status: PlayerStatus,
        displayOrder: Int,
        createdAt: Instant,
        updatedAt: Instant,
    ): RoomPlayerModel =
        object : RoomPlayerModel {
            override val roomPlayerId = roomPlayerId
            override val roomId = roomId
            override val name = name
            override val status = status
            override val displayOrder = displayOrder
            override val createdAt = createdAt
            override val updatedAt = updatedAt
        }
}
