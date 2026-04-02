package com.naminhyeok.fantazzk.template

import com.naminhyeok.fantazzk.template.application.TemplateFinder
import com.naminhyeok.fantazzk.template.application.TemplateFinderImpl
import com.naminhyeok.fantazzk.template.exception.TemplateException
import com.naminhyeok.fantazzk.template.support.InMemoryTemplatePlayerRepository
import com.naminhyeok.fantazzk.template.support.InMemoryTemplateRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TemplateFinderTest {
    private lateinit var templateRepo: InMemoryTemplateRepository
    private lateinit var playerRepo: InMemoryTemplatePlayerRepository
    private lateinit var cut: TemplateFinder

    @BeforeEach
    fun setUp() {
        templateRepo = InMemoryTemplateRepository()
        playerRepo = InMemoryTemplatePlayerRepository()
        cut = TemplateFinderImpl(templateRepo, playerRepo)
    }

    @Test
    fun `존재하지 않는 ID로 상세 조회하면 예외가 발생한다`() {
        assertThatThrownBy { cut.getDetail(TemplateId(999L)) }
            .isInstanceOf(TemplateException.TemplateNotFoundException::class.java)
    }

    @Test
    fun `목록 조회는 저장된 템플릿을 반환한다`() {
        templateRepo.save(
            Template.create(
                name = "첫째",
                configuration = TemplateConfiguration.Auction(teamCount = 2, teamSize = 2, budgetValue = 300),
            ),
        )
        templateRepo.save(
            Template.create(
                name = "둘째",
                configuration = TemplateConfiguration.Draft(teamCount = 2, teamSize = 2, strategy = DraftOrderStrategy.SNAKE),
            ),
        )

        val all = cut.list()
        assertThat(all).hasSize(2)
    }

    @Test
    fun `상세 조회는 템플릿과 선수 목록을 함께 반환한다`() {
        val template =
            templateRepo.save(
                Template.create(
                    name = "테스트",
                    configuration = TemplateConfiguration.Auction(teamCount = 2, teamSize = 2, budgetValue = 300),
                ),
            )
        playerRepo.saveAll(
            listOf(
                TemplatePlayer(templateId = template.templateId, name = "선수1", displayOrder = 0),
                TemplatePlayer(templateId = template.templateId, name = "선수2", displayOrder = 1),
            ),
        )

        val detail = cut.getDetail(TemplateId(template.templateId))

        assertThat(detail.template.templateId).isEqualTo(template.templateId)
        assertThat(detail.players.map { it.name }).containsExactly("선수1", "선수2")
    }

    @Test
    fun `상세 조회는 선수 수가 exact count를 만족하지 않으면 템플릿 invalid 예외를 던진다`() {
        val template =
            templateRepo.save(
                Template.create(
                    name = "테스트",
                    configuration = TemplateConfiguration.Auction(teamCount = 2, teamSize = 2, budgetValue = 300),
                ),
            )
        playerRepo.saveAll(
            listOf(
                TemplatePlayer(templateId = template.templateId, name = "선수1", displayOrder = 0),
            ),
        )

        assertThatThrownBy { cut.getDetail(TemplateId(template.templateId)) }
            .isInstanceOf(TemplateException.TemplateInvalidException::class.java)
    }
}
