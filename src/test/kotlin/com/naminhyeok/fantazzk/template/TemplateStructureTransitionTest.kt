package com.naminhyeok.fantazzk.template

import com.naminhyeok.fantazzk.template.api.TemplateApiController
import com.naminhyeok.fantazzk.template.application.TemplateCreateServiceImpl
import com.naminhyeok.fantazzk.template.application.TemplateFinder
import com.naminhyeok.fantazzk.template.application.TemplateFinderImpl
import com.naminhyeok.fantazzk.template.domain.Template
import com.naminhyeok.fantazzk.template.repository.TemplateRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.lang.reflect.Modifier

class TemplateStructureTransitionTest {
    @Test
    fun `template 리포지토리 공개 surface 는 typed id 계약만 노출한다`() {
        val methods = TemplateRepository::class.java.methods.filter { it.declaringClass == TemplateRepository::class.java }

        assertThat(TemplateRepository::class.java.interfaces.map { it.simpleName }).doesNotContain("JpaRepository")
        assertThat(methods.map { it.name to it.parameterTypes.toList() })
            .contains("save" to listOf(Template::class.java))
            .contains("findById" to listOf(TemplateId::class.java))
            .contains("findAll" to emptyList<Class<*>>())
    }

    @Test
    fun `template API 는 더 이상 query service 에 의존하지 않는다`() {
        val parameterTypes = TemplateApiController::class.java.declaredConstructors.single().parameterTypes.toList()
        assertThat(parameterTypes.map { it.name }).doesNotContain("com.naminhyeok.fantazzk.template.query.TemplateQueryService")
        assertThat(parameterTypes).contains(TemplateFinder::class.java)
    }

    @Test
    fun `template 메인 코드에는 더 이상 spi 와 jdbc 설정 타입이 존재하지 않는다`() {
        listOf(
            "com.naminhyeok.fantazzk.template.spi.TemplateLookup",
            "com.naminhyeok.fantazzk.template.spi.TemplateSpiPackageInfo",
            "com.naminhyeok.fantazzk.template.infrastructure.spi.TemplateLookupAdapter",
            "com.naminhyeok.fantazzk.template.infrastructure.spi.TemplateSpiConfiguration",
            "com.naminhyeok.fantazzk.template.config.TemplateJdbcConfiguration",
        ).forEach { className ->
            assertThatThrownBy { Class.forName(className) }
                .isInstanceOf(ClassNotFoundException::class.java)
        }
    }

    @Test
    fun `template finder 와 create service 는 더 이상 선수 리포지토리에 의존하지 않는다`() {
        val finderDependencies = TemplateFinderImpl::class.java.declaredConstructors.single().parameterTypes.map { it.simpleName }
        val createServiceDependencies = TemplateCreateServiceImpl::class.java.declaredConstructors.single().parameterTypes.map { it.simpleName }

        assertThat(finderDependencies).doesNotContain("TemplatePlayerRepository")
        assertThat(createServiceDependencies).doesNotContain("TemplatePlayerRepository")
    }

    @Test
    fun `template aggregate 생산 모델은 domain 패키지 하나만 사용한다`() {
        assertThat(Class.forName("com.naminhyeok.fantazzk.template.domain.Template")).isNotNull
        assertThat(Class.forName("com.naminhyeok.fantazzk.template.domain.TemplateConfiguration")).isNotNull
        assertThat(Class.forName("com.naminhyeok.fantazzk.template.domain.TemplatePlayer")).isNotNull
        assertThat(runCatching { Class.forName("com.naminhyeok.fantazzk.template.Template") }.isFailure).isTrue()
        assertThat(runCatching { Class.forName("com.naminhyeok.fantazzk.template.TemplateConfiguration") }.isFailure).isTrue()
        assertThat(runCatching { Class.forName("com.naminhyeok.fantazzk.template.TemplatePlayer") }.isFailure).isTrue()
    }

    @Test
    fun `template aggregate 는 선수 없는 공개 생성 경로를 노출하지 않는다`() {
        val publicConstructors =
            Template::class.java.declaredConstructors.filter {
                Modifier.isPublic(it.modifiers) && !it.isSynthetic
            }

        assertThat(publicConstructors).isEmpty()
    }
}
