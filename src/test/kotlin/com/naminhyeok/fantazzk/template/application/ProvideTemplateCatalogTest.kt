package com.naminhyeok.fantazzk.template.application

import com.naminhyeok.fantazzk.template.TemplateCatalogException
import com.naminhyeok.fantazzk.template.TemplateDraftOrderStrategy
import com.naminhyeok.fantazzk.template.TemplateId
import com.naminhyeok.fantazzk.template.TemplateMode
import com.naminhyeok.fantazzk.template.domain.DraftOrderStrategy
import com.naminhyeok.fantazzk.template.domain.Template
import com.naminhyeok.fantazzk.template.exception.TemplateException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class ProvideTemplateCatalogTest {
    private val templateFinder: FindTemplates = mockk()
    private val cut = ProvideTemplateCatalog(templateFinder)

    @Test
    fun `TemplateId를 그대로 finder에 전달하고 blueprint로 변환한다`() {
        val template =
            Template.createDraft("드래프트 템플릿", 2, 3, DraftOrderStrategy.SNAKE, listOf("선수1", "선수2", "선수3", "선수4"))
        val templateId = template.id
        every { templateFinder.getDetail(templateId) } returns TemplateDetail(template, template.players())

        val blueprint = cut.getTemplateBlueprint(templateId)

        assertThat(blueprint.templateId).isEqualTo(templateId)
        assertThat(blueprint.mode).isEqualTo(TemplateMode.DRAFT)
        assertThat(blueprint.draftOrderStrategy).isEqualTo(TemplateDraftOrderStrategy.SNAKE)
        assertThat(blueprint.players.map { it.name }).containsExactly("선수1", "선수2", "선수3", "선수4")
        verify(exactly = 1) { templateFinder.getDetail(templateId) }
    }

    @Test
    fun `템플릿 없음 예외를 같은 TemplateId의 catalog 예외로 변환한다`() {
        val templateId = TemplateId.of("00000000-0000-0000-0000-000000000099")
        every { templateFinder.getDetail(templateId) } throws TemplateException.TemplateNotFoundException()

        assertThatThrownBy { cut.getTemplateBlueprint(templateId) }
            .isInstanceOfSatisfying(TemplateCatalogException.NotFound::class.java) {
                assertThat(it.templateId).isEqualTo(templateId)
            }
    }

    @Test
    fun `유효하지 않은 템플릿 예외를 같은 TemplateId의 catalog 예외로 변환한다`() {
        val templateId = TemplateId.of("00000000-0000-0000-0000-000000000100")
        every { templateFinder.getDetail(templateId) } throws TemplateException.TemplateInvalidException()

        assertThatThrownBy { cut.getTemplateBlueprint(templateId) }
            .isInstanceOfSatisfying(TemplateCatalogException.Invalid::class.java) {
                assertThat(it.templateId).isEqualTo(templateId)
            }
    }
}
