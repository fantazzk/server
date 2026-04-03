package com.naminhyeok.fantazzk.template

import com.naminhyeok.fantazzk.template.application.TemplateFinder
import com.naminhyeok.fantazzk.template.domain.Template
import com.naminhyeok.fantazzk.template.exception.TemplateException
import com.naminhyeok.fantazzk.template.repository.TemplateRepository
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.dao.InvalidDataAccessApiUsageException

class TemplateFinderTest {
    private lateinit var templateRepo: TemplateRepository
    private lateinit var cut: TemplateFinder

    @BeforeEach
    fun setUp() {
        templateRepo = mockk()
        cut = TemplateFinder(templateRepo)
    }

    @Test
    fun `존재하지 않는 ID로 상세 조회하면 예외가 발생한다`() {
        every { templateRepo.findById(TemplateId(999L)) } returns null

        assertThatThrownBy { cut.getDetail(TemplateId(999L)) }
            .isInstanceOf(TemplateException.TemplateNotFoundException::class.java)
    }

    @Test
    fun `목록 조회는 저장된 템플릿을 반환한다`() {
        every { templateRepo.findAll() } returns
            listOf(
                Template.createAuction(
                    name = "첫째",
                    teamCount = 2,
                    teamSize = 2,
                    budget = 300,
                    playerNames = listOf("선수1", "선수2"),
                ).assignId(TemplateId(1L)),
                Template.createDraft(
                    name = "둘째",
                    teamCount = 2,
                    teamSize = 2,
                    strategy = DraftOrderStrategy.SNAKE,
                    playerNames = listOf("선수1", "선수2"),
                ).assignId(TemplateId(2L)),
            )

        val all = cut.list()
        assertThat(all).hasSize(2)
    }

    @Test
    fun `상세 조회는 템플릿과 선수 목록을 함께 반환한다`() {
        val template =
            Template.createAuction(
                name = "첫째",
                teamCount = 2,
                teamSize = 2,
                budget = 300,
                playerNames = listOf("선수1", "선수2"),
            ).assignId(TemplateId(1L))
        every { templateRepo.findById(TemplateId(template.templateId)) } returns template

        val detail = cut.getDetail(TemplateId(template.templateId))

        assertThat(detail.template.templateId).isEqualTo(template.templateId)
        assertThat(detail.players.map { it.name }).containsExactly("선수1", "선수2")
    }

    @Test
    fun `상세 조회는 저장소가 유효하지 않은 aggregate를 로드하면 템플릿 invalid 예외를 던진다`() {
        every { templateRepo.findById(TemplateId(1L)) } throws InvalidDataAccessApiUsageException("선수 수는 정확히 2명이어야 합니다")

        assertThatThrownBy { cut.getDetail(TemplateId(1L)) }
            .isInstanceOf(TemplateException.TemplateInvalidException::class.java)
    }
}
