package com.naminhyeok.fantazzk.template

import com.naminhyeok.fantazzk.template.api.TemplateApiController
import com.naminhyeok.fantazzk.template.application.TemplateCreateServiceImpl
import com.naminhyeok.fantazzk.template.application.TemplateFinder
import com.naminhyeok.fantazzk.template.application.TemplateFinderImpl
import com.naminhyeok.fantazzk.template.domain.Template
import com.naminhyeok.fantazzk.template.repository.TemplateRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.Optional

class TemplateStructureTransitionTest {
    @Test
    fun `template 리포지토리는 TemplateId 를 기준으로 동작한다`() {
        assertThat(
            TemplateRepository::class.java.getMethod("findById", TemplateId::class.java).returnType,
        ).isEqualTo(Optional::class.java)
    }

    @Test
    fun `template API 는 더 이상 query service 에 의존하지 않는다`() {
        val parameterTypes = TemplateApiController::class.java.declaredConstructors.single().parameterTypes.toList()
        assertThat(parameterTypes.map { it.name }).doesNotContain("com.naminhyeok.fantazzk.template.query.TemplateQueryService")
        assertThat(parameterTypes).contains(TemplateFinder::class.java)
    }

    @Test
    fun `template finder 와 create service 는 더 이상 선수 리포지토리에 의존하지 않는다`() {
        val finderDependencies = TemplateFinderImpl::class.java.declaredConstructors.single().parameterTypes.map { it.simpleName }
        val createServiceDependencies = TemplateCreateServiceImpl::class.java.declaredConstructors.single().parameterTypes.map { it.simpleName }

        assertThat(finderDependencies).doesNotContain("TemplatePlayerRepository")
        assertThat(createServiceDependencies).doesNotContain("TemplatePlayerRepository")
    }
}
