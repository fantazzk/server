package com.naminhyeok.fantazzk.room

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class RoomPlayerTest {
    @ParameterizedTest(name = "{0} 상태의 선수 isAvailable 검증")
    @EnumSource(PlayerStatus::class)
    fun `isAvailable은 AVAILABLE 상태에서만 true를 반환한다`(status: PlayerStatus) {
        val player = RoomPlayer(roomId = 1L, name = "선수1", status = status, displayOrder = 0)
        assertThat(player.isAvailable()).isEqualTo(status == PlayerStatus.AVAILABLE)
    }
}
