package com.naminhyeok.fantazzk.teambuilding.room

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PlayerPoolTest {
    private val pool =
        PlayerPool(
            players =
                listOf(
                    Player("선수1"),
                    Player("선수2"),
                    Player("선수3"),
                ),
        )

    @Test
    fun `현재 경매 대상은 첫 번째 가용 선수이다`() {
        assertThat(pool.currentTarget()?.name).isEqualTo("선수1")
    }

    @Test
    fun `선수를 배정하면 ASSIGNED 상태로 변경된다`() {
        val updated = pool.assignPlayer("선수1")

        assertThat(updated.players.first { it.name == "선수1" }.status).isEqualTo(PlayerStatus.ASSIGNED)
        assertThat(updated.currentTarget()?.name).isEqualTo("선수2")
    }

    @Test
    fun `유찰 시 현재 대상 선수가 맨 뒤로 이동한다`() {
        val updated = pool.moveCurrentToBack()
        val available = updated.players.filter { it.status == PlayerStatus.AVAILABLE }

        assertThat(available.first().name).isEqualTo("선수2")
        assertThat(available.last().name).isEqualTo("선수1")
    }

    @Test
    fun `모든 선수가 배정되면 현재 대상은 null이다`() {
        val empty =
            pool
                .assignPlayer("선수1")
                .assignPlayer("선수2")
                .assignPlayer("선수3")

        assertThat(empty.currentTarget()).isNull()
    }
}
