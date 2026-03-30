package com.naminhyeok.fantazzk.integration.roomtemplate

import com.naminhyeok.fantazzk.room.DraftOrderStrategy
import com.naminhyeok.fantazzk.room.TeamBuildingMode
import com.naminhyeok.fantazzk.room.outport.TemplateLookupPortException
import com.naminhyeok.fantazzk.template.TemplateIdentity
import com.naminhyeok.fantazzk.template.TemplateLookupService
import com.naminhyeok.fantazzk.template.TemplateModel
import com.naminhyeok.fantazzk.template.TemplatePlayer
import com.naminhyeok.fantazzk.template.of
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TemplateLookupAdapterTest {
    private val templateLookupService: TemplateLookupService = mockk()
    private lateinit var cut: TemplateLookupAdapter

    @BeforeEach
    fun setUp() {
        cut = TemplateLookupAdapter(templateLookupService)
    }

    @Test
    fun `find가 null이면 room 포트 not found 예외로 번역한다`() {
        every { templateLookupService.find(TemplateIdentity.of(999L)) } returns null

        assertThatThrownBy { cut.getTemplate(999L) }
            .isInstanceOf(TemplateLookupPortException.NotFound::class.java)
            .hasMessage("템플릿을 찾을 수 없습니다")
    }

    @Test
    fun `find와 getPlayers 결과를 room 포트 snapshot으로 변환한다`() {
        every { templateLookupService.find(TemplateIdentity.of(1L)) } returns templateModel()
        every { templateLookupService.getPlayers(1L) } returns listOf(TemplatePlayer(templateId = 1L, name = "선수1", displayOrder = 0))

        val snapshot = cut.getTemplate(1L)

        assertThat(snapshot.mode).isEqualTo(TeamBuildingMode.DRAFT)
        assertThat(snapshot.teamCount).isEqualTo(1)
        assertThat(snapshot.teamSize).isEqualTo(2)
        assertThat(snapshot.budget).isNull()
        assertThat(snapshot.draftOrderStrategy).isEqualTo(DraftOrderStrategy.SNAKE)
        assertThat(snapshot.players.single().name).isEqualTo("선수1")
        assertThat(snapshot.players.single().displayOrder).isEqualTo(0)
    }

    @Test
    fun `모드별 필드 조합이 잘못된 템플릿은 invalid 예외로 번역한다`() {
        every { templateLookupService.find(TemplateIdentity.of(1L)) } returns invalidAuctionTemplateModel()
        every { templateLookupService.getPlayers(1L) } returns listOf(TemplatePlayer(templateId = 1L, name = "선수1", displayOrder = 0))

        assertThatThrownBy { cut.getTemplate(1L) }
            .isInstanceOf(TemplateLookupPortException.Invalid::class.java)
            .hasMessage("유효하지 않은 템플릿입니다")
    }

    @Test
    fun `선수 수가 exact count를 만족하지 않으면 invalid 예외로 번역한다`() {
        every { templateLookupService.find(TemplateIdentity.of(1L)) } returns exactCountTemplateModel()
        every { templateLookupService.getPlayers(1L) } returns listOf(TemplatePlayer(templateId = 1L, name = "선수1", displayOrder = 0))

        assertThatThrownBy { cut.getTemplate(1L) }
            .isInstanceOf(TemplateLookupPortException.Invalid::class.java)
            .hasMessage("유효하지 않은 템플릿입니다")
    }

    private fun templateModel(): TemplateModel =
        object : TemplateModel {
            override val templateId: Long = 1L
            override val name: String = "드래프트 템플릿"
            override val mode: com.naminhyeok.fantazzk.template.TeamBuildingMode = com.naminhyeok.fantazzk.template.TeamBuildingMode.DRAFT
            override val teamCount: Int = 1
            override val teamSize: Int = 2
            override val budget: Int? = null
            override val draftOrderStrategy: com.naminhyeok.fantazzk.template.DraftOrderStrategy? =
                com.naminhyeok.fantazzk.template.DraftOrderStrategy.SNAKE
            override val createdAt = java.time.Instant.parse("2025-01-01T00:00:00Z")
            override val updatedAt = java.time.Instant.parse("2025-01-01T00:00:00Z")
        }

    private fun invalidAuctionTemplateModel(): TemplateModel =
        object : TemplateModel {
            override val templateId: Long = 1L
            override val name: String = "깨진 경매 템플릿"
            override val mode = com.naminhyeok.fantazzk.template.TeamBuildingMode.AUCTION
            override val teamCount: Int = 2
            override val teamSize: Int = 2
            override val budget: Int? = null
            override val draftOrderStrategy: com.naminhyeok.fantazzk.template.DraftOrderStrategy? = null
            override val createdAt = java.time.Instant.parse("2025-01-01T00:00:00Z")
            override val updatedAt = java.time.Instant.parse("2025-01-01T00:00:00Z")
        }

    private fun exactCountTemplateModel(): TemplateModel =
        object : TemplateModel {
            override val templateId: Long = 1L
            override val name: String = "정상 템플릿"
            override val mode = com.naminhyeok.fantazzk.template.TeamBuildingMode.AUCTION
            override val teamCount: Int = 2
            override val teamSize: Int = 2
            override val budget: Int? = 300
            override val draftOrderStrategy: com.naminhyeok.fantazzk.template.DraftOrderStrategy? = null
            override val createdAt = java.time.Instant.parse("2025-01-01T00:00:00Z")
            override val updatedAt = java.time.Instant.parse("2025-01-01T00:00:00Z")
        }
}
