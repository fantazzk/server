@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.domain.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
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
    inner class `상태 전이` {
        @Test
        fun `선수를 배정하면 ASSIGNED 상태가 된다`() {
            val player = RoomPlayer(roomId = 1L, name = "선수1", displayOrder = 0)

            val assigned = player.assign()

            assertThat(assigned.status).isEqualTo(PlayerStatus.ASSIGNED)
            assertThat(assigned.displayOrder).isEqualTo(player.displayOrder)
        }

        @Test
        fun `이미 배정된 선수는 다시 배정할 수 없다`() {
            val player = RoomPlayer(roomId = 1L, name = "선수1", status = PlayerStatus.ASSIGNED, displayOrder = 0)

            assertThatThrownBy { player.assign() }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessageContaining("선수를 배정할 수 없습니다")
        }

        @Test
        fun `선수를 뒤로 보내면 순서만 갱신되고 상태는 유지된다`() {
            val player = RoomPlayer(roomId = 1L, name = "선수1", displayOrder = 0)

            val moved = player.moveToBack(3)

            assertThat(moved.displayOrder).isEqualTo(3)
            assertThat(moved.status).isEqualTo(PlayerStatus.AVAILABLE)
        }

        @Test
        fun `선수는 현재 순서보다 뒤로만 이동할 수 있다`() {
            val player = RoomPlayer(roomId = 1L, name = "선수1", displayOrder = 3)

            assertThatThrownBy { player.moveToBack(3) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("현재 순서보다 뒤로만 이동할 수 있습니다")
        }

        @Test
        fun `선수는 음수 순서로 이동할 수 없다`() {
            val player = RoomPlayer(roomId = 1L, name = "선수1", displayOrder = 3)

            assertThatThrownBy { player.moveToBack(-1) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("순서는 0 이상이어야 합니다")
        }

        @Test
        fun `배정된 선수는 뒤로 보낼 수 없다`() {
            val player = RoomPlayer(roomId = 1L, name = "선수1", status = PlayerStatus.ASSIGNED, displayOrder = 3)

            assertThatThrownBy { player.moveToBack(4) }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessageContaining("선수를 뒤로 보낼 수 없습니다")
        }
    }
}
