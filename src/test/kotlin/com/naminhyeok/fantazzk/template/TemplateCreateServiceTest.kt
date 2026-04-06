package com.naminhyeok.fantazzk.template

import com.naminhyeok.fantazzk.template.application.CreateTemplate
import com.naminhyeok.fantazzk.template.application.CreateTemplateCommand
import com.naminhyeok.fantazzk.template.domain.DraftOrderStrategy
import com.naminhyeok.fantazzk.template.domain.Template
import com.naminhyeok.fantazzk.template.domain.TemplateConfiguration
import com.naminhyeok.fantazzk.template.repository.Templates
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TemplateCreateServiceTest {
    private lateinit var templateRepo: Templates
    private lateinit var cut: CreateTemplate

    @BeforeEach
    fun setUp() {
        templateRepo = mockk()
        every { templateRepo.save(any<Template>()) } answers { firstArg() }
        cut = CreateTemplate(templateRepo)
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
            .isEqualTo(TemplateConfiguration.auction(teamCount = 2, teamSize = 2, budget = 500))

        val players = template.players()
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
            .isEqualTo(TemplateConfiguration.draft(teamCount = 2, teamSize = 2, strategy = DraftOrderStrategy.SNAKE))
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
