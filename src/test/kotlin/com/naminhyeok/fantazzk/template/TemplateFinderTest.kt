package com.naminhyeok.fantazzk.template

import com.naminhyeok.fantazzk.template.application.FindTemplates
import com.naminhyeok.fantazzk.template.domain.DraftOrderStrategy
import com.naminhyeok.fantazzk.template.domain.Template
import com.naminhyeok.fantazzk.template.exception.TemplateException
import com.naminhyeok.fantazzk.template.repository.Templates
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.dao.InvalidDataAccessApiUsageException

class TemplateFinderTest {
    private lateinit var templateRepo: Templates
    private lateinit var cut: FindTemplates

    @BeforeEach
    fun setUp() {
        templateRepo = mockk()
        cut = FindTemplates(templateRepo)
    }

    @Test
    fun `존재하지 않는 ID로 상세 조회하면 예외가 발생한다`() {
        every { templateRepo.findById(templateId(999)) } returns null

        assertThatThrownBy { cut.getDetail(templateId(999)) }
            .isInstanceOf(TemplateException.TemplateNotFoundException::class.java)
    }

    @Test
    fun `목록 조회는 저장된 템플릿을 반환한다`() {
        every { templateRepo.findAll() } returns
            listOf(
                Template.createAuction("첫째", 2, 2, 300, listOf("선수1", "선수2")).assignId(templateId(1)),
                Template.createDraft("둘째", 2, 2, DraftOrderStrategy.SNAKE, listOf("선수1", "선수2")).assignId(templateId(2)),
            )

        val all = cut.list()
        assertThat(all).hasSize(2)
    }

    @Test
    fun `상세 조회는 템플릿과 선수 목록을 함께 반환한다`() {
        val template =
            Template.createAuction("첫째", 2, 2, 300, listOf("선수1", "선수2")).assignId(templateId(1))
        every { templateRepo.findById(template.templateId) } returns template

        val detail = cut.getDetail(template.templateId)

        assertThat(detail.template.templateId).isEqualTo(template.templateId)
        assertThat(detail.players.map { it.name }).containsExactly("선수1", "선수2")
    }

    @Test
    fun `상세 조회는 저장소가 유효하지 않은 aggregate를 로드하면 템플릿 invalid 예외를 던진다`() {
        every { templateRepo.findById(templateId(1)) } throws InvalidDataAccessApiUsageException("선수 수는 정확히 2명이어야 합니다")

        assertThatThrownBy { cut.getDetail(templateId(1)) }
            .isInstanceOf(TemplateException.TemplateInvalidException::class.java)
    }

    private fun templateId(number: Long): TemplateId = TemplateId.from(templateIdText(number))

    private fun templateIdText(number: Long): String = "00000000-0000-0000-0000-${number.toString().padStart(12, '0')}"
}
