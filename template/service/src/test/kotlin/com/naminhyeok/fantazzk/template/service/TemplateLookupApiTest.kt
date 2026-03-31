package com.naminhyeok.fantazzk.template.service

import com.naminhyeok.fantazzk.template.api.TemplateDraftStrategy
import com.naminhyeok.fantazzk.template.api.TemplateLookup
import com.naminhyeok.fantazzk.template.api.TemplateLookupException
import com.naminhyeok.fantazzk.template.api.TemplateMode
import com.naminhyeok.fantazzk.template.api.TemplatePlayerView
import com.naminhyeok.fantazzk.template.model.TemplateIdentity
import com.naminhyeok.fantazzk.template.model.TemplateModel
import com.naminhyeok.fantazzk.template.model.TemplatePlayer
import com.naminhyeok.fantazzk.template.model.of
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import com.naminhyeok.fantazzk.template.model.DraftOrderStrategy as TemplateDraftOrderStrategy
import com.naminhyeok.fantazzk.template.model.TeamBuildingMode as TemplateTeamBuildingMode

class TemplateLookupApiTest {
    private val templateLookupService: TemplateLookupService = mockk()
    private lateinit var cut: TemplateLookup

    @BeforeEach
    fun setUp() {
        cut = TemplateLookupFacade(templateLookupService)
    }

    @Test
    fun `find가 null이면 lookup not found 예외로 번역한다`() {
        every { templateLookupService.find(TemplateIdentity.of(999L)) } returns null

        assertThatThrownBy { cut.get(999L) }
            .isInstanceOf(TemplateLookupException.NotFound::class.java)
            .hasMessage("템플릿을 찾을 수 없습니다")
    }

    @Test
    fun `find와 getPlayers 결과를 lookup view로 변환한다`() {
        every { templateLookupService.find(TemplateIdentity.of(1L)) } returns templateModel()
        every { templateLookupService.getPlayers(1L) } returns listOf(TemplatePlayer(templateId = 1L, name = "선수1", displayOrder = 0))

        val template = cut.get(1L)

        assertThat(template.mode).isEqualTo(TemplateMode.DRAFT)
        assertThat(template.teamCount).isEqualTo(1)
        assertThat(template.teamSize).isEqualTo(2)
        assertThat(template.budget).isNull()
        assertThat(template.draftOrderStrategy).isEqualTo(TemplateDraftStrategy.SNAKE)
        assertThat(template.players.single()).isEqualTo(TemplatePlayerView(name = "선수1", displayOrder = 0))
    }

    @Test
    fun `선수 수가 exact count를 만족하지 않으면 invalid 예외로 번역한다`() {
        every { templateLookupService.find(TemplateIdentity.of(1L)) } returns exactCountTemplateModel()
        every { templateLookupService.getPlayers(1L) } returns listOf(TemplatePlayer(templateId = 1L, name = "선수1", displayOrder = 0))

        assertThatThrownBy { cut.get(1L) }
            .isInstanceOf(TemplateLookupException.Invalid::class.java)
            .hasMessage("유효하지 않은 템플릿입니다")
    }

    private fun templateModel(): TemplateModel =
        object : TemplateModel {
            override val templateId: Long = 1L
            override val name: String = "드래프트 템플릿"
            override val mode: TemplateTeamBuildingMode = TemplateTeamBuildingMode.DRAFT
            override val teamCount: Int = 1
            override val teamSize: Int = 2
            override val budget: Int? = null
            override val draftOrderStrategy: TemplateDraftOrderStrategy? = TemplateDraftOrderStrategy.SNAKE
            override val createdAt = java.time.Instant.parse("2025-01-01T00:00:00Z")
            override val updatedAt = java.time.Instant.parse("2025-01-01T00:00:00Z")
        }

    private fun exactCountTemplateModel(): TemplateModel =
        object : TemplateModel {
            override val templateId: Long = 1L
            override val name: String = "정상 템플릿"
            override val mode = TemplateTeamBuildingMode.AUCTION
            override val teamCount: Int = 2
            override val teamSize: Int = 2
            override val budget: Int? = 300
            override val draftOrderStrategy: TemplateDraftOrderStrategy? = null
            override val createdAt = java.time.Instant.parse("2025-01-01T00:00:00Z")
            override val updatedAt = java.time.Instant.parse("2025-01-01T00:00:00Z")
        }
}
