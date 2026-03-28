package com.naminhyeok.fantazzk.teambuilding.template

import com.naminhyeok.fantazzk.teambuilding.TeamBuildingMode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class TemplateTest {
    @Test
    fun `경매 템플릿을 생성할 수 있다`() {
        val template =
            Template(
                name = "자낳대 시즌7 경매",
                mode = TeamBuildingMode.AUCTION,
                teamCount = 5,
                teamSize = 5,
                budget = 300,
            )

        assertThat(template.mode).isEqualTo(TeamBuildingMode.AUCTION)
        assertThat(template.budget).isEqualTo(300)
        assertThat(template.picksPerTeam).isEqualTo(4)
    }

    @Test
    fun `팀 수는 0 이하일 수 없다`() {
        assertThatThrownBy { Template(name = "test", mode = TeamBuildingMode.AUCTION, teamCount = 0, teamSize = 5) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `팀 인원은 0 이하일 수 없다`() {
        assertThatThrownBy { Template(name = "test", mode = TeamBuildingMode.AUCTION, teamCount = 5, teamSize = 0) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `예산이 지정되면 0 이하일 수 없다`() {
        assertThatThrownBy { Template(name = "test", mode = TeamBuildingMode.AUCTION, teamCount = 5, teamSize = 5, budget = 0) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `TemplateIdentity를 생성할 수 있다`() {
        val identity = TemplateIdentity.of(1L)
        assertThat(identity.templateId).isEqualTo(1L)
    }

    @Test
    fun `TemplatePlayer를 생성할 수 있다`() {
        val player = TemplatePlayer(templateId = 1L, name = "선수1", displayOrder = 0)
        assertThat(player.name).isEqualTo("선수1")
        assertThat(player.displayOrder).isEqualTo(0)
    }
}
