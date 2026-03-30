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
    fun `경매 생성 command로 템플릿과 선수 컬렉션을 저장한다`() {
        val template =
            cut.create(
                CreateTemplateCommand.Auction(
                    name = "경매전",
                    teamCount = 2,
                    teamSize = 2,
                    budget = 500,
                    playerNames = listOf("선수A", "선수B"),
                ),
            )

        assertThat(template.name).isEqualTo("경매전")
        assertThat(template.configuration)
            .isEqualTo(TemplateConfiguration.Auction(teamCount = 2, teamSize = 2, budgetValue = 500))

        val players = playerRepo.findByTemplateId(template.templateId)
        assertThat(players.map { it.name }).containsExactly("선수A", "선수B")
        assertThat(players.map { it.displayOrder }).containsExactly(0, 1)
    }

    @Test
    fun `드래프트 생성 command로 드래프트 설정을 저장한다`() {
        val template =
            cut.create(
                CreateTemplateCommand.Draft(
                    name = "드래프트전",
                    teamCount = 2,
                    teamSize = 2,
                    strategy = DraftOrderStrategy.SNAKE,
                    playerNames = listOf("선수1", "선수2"),
                ),
            )

        assertThat(template.configuration)
            .isEqualTo(TemplateConfiguration.Draft(teamCount = 2, teamSize = 2, strategy = DraftOrderStrategy.SNAKE))
        assertThat(template.budget).isNull()
    }

    @Test
    fun `서비스는 exact player count를 강제한다`() {
        assertThatThrownBy {
            cut.create(
                CreateTemplateCommand.Auction(
                    name = "실패",
                    teamCount = 2,
                    teamSize = 2,
                    budget = 300,
                    playerNames = listOf("선수1"),
                ),
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("선수 수는 정확히 2명이어야 합니다")
    }
}
