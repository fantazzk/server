package com.naminhyeok.fantazzk.teambuilding.room

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
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
    fun `currentTarget returns first available player`() {
        assertEquals("선수1", pool.currentTarget()?.name)
    }

    @Test
    fun `assignPlayer marks player as ASSIGNED and returns updated pool`() {
        val updated = pool.assignPlayer("선수1")
        assertEquals(PlayerStatus.ASSIGNED, updated.players.first { it.name == "선수1" }.status)
        assertEquals("선수2", updated.currentTarget()?.name)
    }

    @Test
    fun `moveCurrentToBack moves first available player to end`() {
        val updated = pool.moveCurrentToBack()
        val available = updated.players.filter { it.status == PlayerStatus.AVAILABLE }
        assertEquals("선수2", available.first().name)
        assertEquals("선수1", available.last().name)
    }

    @Test
    fun `currentTarget returns null when no available players`() {
        val empty =
            pool
                .assignPlayer("선수1")
                .assignPlayer("선수2")
                .assignPlayer("선수3")
        assertNull(empty.currentTarget())
    }
}
