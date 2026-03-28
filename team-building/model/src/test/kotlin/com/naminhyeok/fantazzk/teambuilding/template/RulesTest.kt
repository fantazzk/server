package com.naminhyeok.fantazzk.teambuilding.template

import com.naminhyeok.fantazzk.teambuilding.DraftOrderStrategy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class RulesTest {
    @Test
    fun `teamCount must be positive`() {
        assertThrows<IllegalArgumentException> {
            Rules(teamCount = 0, teamSize = 5)
        }
    }

    @Test
    fun `teamSize must be positive`() {
        assertThrows<IllegalArgumentException> {
            Rules(teamCount = 5, teamSize = 0)
        }
    }

    @Test
    fun `budget must be positive when provided`() {
        assertThrows<IllegalArgumentException> {
            Rules(teamCount = 5, teamSize = 5, budget = 0)
        }
    }

    @Test
    fun `valid auction rules`() {
        assertDoesNotThrow {
            Rules(teamCount = 5, teamSize = 5, budget = 300)
        }
    }

    @Test
    fun `valid draft rules`() {
        assertDoesNotThrow {
            Rules(teamCount = 5, teamSize = 5, draftOrderStrategy = DraftOrderStrategy.SNAKE)
        }
    }
}
