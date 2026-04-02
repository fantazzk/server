package com.naminhyeok.fantazzk.template

import com.naminhyeok.fantazzk.template.api.TemplateApiController
import com.naminhyeok.fantazzk.template.query.TemplateQueryService
import com.naminhyeok.fantazzk.template.repository.TemplateRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TemplateStructureTransitionTest {
    @Test
    fun `template 리포지토리와 조회 서비스는 concrete domain type 과 TemplateId 를 사용한다`() {
        assertThat(
            TemplateRepository::class.java.getMethod("findById", TemplateId::class.java).returnType,
        ).isEqualTo(Template::class.java)
    }

    @Test
    fun `template API 는 더 이상 query service 에 의존하지 않는다`() {
        val parameterTypes = TemplateApiController::class.java.declaredConstructors.single().parameterTypes.toList()
        assertThat(parameterTypes).doesNotContain(TemplateQueryService::class.java)
    }
}
