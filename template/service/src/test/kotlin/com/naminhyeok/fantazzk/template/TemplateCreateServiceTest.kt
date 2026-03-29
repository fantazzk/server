package com.naminhyeok.fantazzk.template

import com.naminhyeok.fantazzk.template.support.InMemoryTemplatePlayerRepository
import com.naminhyeok.fantazzk.template.support.InMemoryTemplateRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TemplateCreateServiceTest {
    private lateinit var templateRepo: InMemoryTemplateRepository
    private lateinit var playerRepo: InMemoryTemplatePlayerRepository
    private lateinit var cut: TemplateCreateService

    @BeforeEach
    fun setUp() {
        templateRepo = InMemoryTemplateRepository()
        playerRepo = InMemoryTemplatePlayerRepository()
        cut = TemplateCreateServiceImpl(templateRepo, playerRepo)
    }

    @Test
    fun `경매 템플릿을 생성할 수 있다`() {
        val template = cut.create("경매전", TeamBuildingMode.AUCTION, 2, 3, 500, null, listOf("선수1", "선수2"))

        assertThat(template.name).isEqualTo("경매전")
        assertThat(template.mode).isEqualTo(TeamBuildingMode.AUCTION)
        assertThat(template.budget).isEqualTo(500)
    }

    @Test
    fun `드래프트 템플릿을 생성할 수 있다`() {
        val template = cut.create("드래프트", TeamBuildingMode.DRAFT, 2, 2, null, DraftOrderStrategy.SNAKE, listOf("선수1"))

        assertThat(template.mode).isEqualTo(TeamBuildingMode.DRAFT)
        assertThat(template.draftOrderStrategy).isEqualTo(DraftOrderStrategy.SNAKE)
        assertThat(template.budget).isNull()
    }

    @Test
    fun `템플릿 생성 시 선수 목록이 순서대로 저장된다`() {
        val template = cut.create("테스트", TeamBuildingMode.AUCTION, 2, 2, 300, null, listOf("A", "B", "C"))

        val players = playerRepo.findByTemplateId(template.templateId)
        assertThat(players).hasSize(3)
        assertThat(players.map { it.name }).containsExactly("A", "B", "C")
        assertThat(players.map { it.displayOrder }).containsExactly(0, 1, 2)
    }

    @Test
    fun `팀 수가 0이면 생성할 수 없다`() {
        assertThatThrownBy {
            cut.create("실패", TeamBuildingMode.AUCTION, 0, 2, 300, null, emptyList())
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `팀 인원이 0이면 생성할 수 없다`() {
        assertThatThrownBy {
            cut.create("실패", TeamBuildingMode.AUCTION, 2, 0, 300, null, emptyList())
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `예산이 0이면 생성할 수 없다`() {
        assertThatThrownBy {
            cut.create("실패", TeamBuildingMode.AUCTION, 2, 2, 0, null, emptyList())
        }.isInstanceOf(IllegalArgumentException::class.java)
    }
}
