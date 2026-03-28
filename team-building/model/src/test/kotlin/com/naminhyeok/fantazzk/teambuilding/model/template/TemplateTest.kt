package com.naminhyeok.fantazzk.teambuilding.model.template

import com.naminhyeok.fantazzk.teambuilding.model.TeamBuildingMode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TemplateTest {
    @Test
    fun `create auction template`() {
        val template = Template(
            id = TemplateId(1L),
            name = "자낳대 시즌7 경매",
            mode = TeamBuildingMode.AUCTION,
            rules = Rules(teamCount = 5, teamSize = 5, budget = 300),
            players = listOf(
                PlayerEntry("선수1"),
                PlayerEntry("선수2", mapOf("tier" to "S")),
            ),
        )

        assertEquals(TeamBuildingMode.AUCTION, template.mode)
        assertEquals(2, template.players.size)
        assertEquals(300, template.rules.budget)
    }
}
