package com.naminhyeok.fantazzk.template.query

import com.naminhyeok.fantazzk.template.TeamBuildingMode
import com.naminhyeok.fantazzk.template.TemplateCreated
import com.naminhyeok.fantazzk.template.TemplatePlayerCreated
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TemplateProjectionUpdaterTest {
    private lateinit var writer: InMemoryTemplateProjectionWriter
    private lateinit var cut: TemplateProjectionUpdater

    @BeforeEach
    fun setUp() {
        writer = InMemoryTemplateProjectionWriter()
        cut = TemplateProjectionUpdater(writer)
    }

    @Test
    fun `TemplateCreated event로 템플릿 projection을 만든다`() {
        cut.on(
            TemplateCreated(
                templateId = 1L,
                name = "테스트",
                mode = TeamBuildingMode.AUCTION,
                teamCount = 2,
                teamSize = 2,
                budget = 300,
                draftOrderStrategy = null,
                players = listOf(TemplatePlayerCreated(name = "선수1", displayOrder = 0)),
            ),
        )

        val template = writer.templates[1L]
        assertThat(template).isNotNull
        assertThat(template!!.name).isEqualTo("테스트")
        val player = writer.players.filter { it.templateId == 1L }.single()
        assertThat(player.name).isEqualTo("선수1")
    }

    private class InMemoryTemplateProjectionWriter : TemplateProjectionWriter(mockk()) {
        val templates = linkedMapOf<Long, TemplateViewEntity>()
        val players = mutableListOf<TemplatePlayerViewEntity>()
        private var seq = 1L

        override fun upsertTemplate(
            templateId: Long,
            name: String,
            mode: TeamBuildingMode,
            teamCount: Int,
            teamSize: Int,
            budget: Int?,
            draftOrderStrategy: com.naminhyeok.fantazzk.template.DraftOrderStrategy?,
        ) {
            templates[templateId] =
                TemplateViewEntity(
                    templateId = templateId,
                    name = name,
                    mode = mode,
                    teamCount = teamCount,
                    teamSize = teamSize,
                    budget = budget,
                    draftOrderStrategy = draftOrderStrategy,
                )
        }

        override fun replacePlayers(
            templateId: Long,
            players: List<TemplatePlayerView>,
        ) {
            this.players.removeIf { it.templateId == templateId }
            this.players +=
                players.map {
                    TemplatePlayerViewEntity(
                        id = seq++,
                        templateId = templateId,
                        name = it.name,
                        displayOrder = it.displayOrder,
                    )
                }
        }
    }
}
