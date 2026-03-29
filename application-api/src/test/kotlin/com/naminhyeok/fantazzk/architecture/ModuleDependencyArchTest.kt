package com.naminhyeok.fantazzk.architecture

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.reflect.KVisibility

class ModuleDependencyArchTest {
    private val classes: JavaClasses =
        ClassFileImporter()
            .withImportOption(ImportOption.DoNotIncludeTests())
            .importPackages("com.naminhyeok.fantazzk")

    @Test
    fun `아키텍처 검증 대상 프로덕션 클래스가 비어있지 않다`() {
        assertThat(classes).isNotEmpty()
    }

    @Test
    fun `컴포넌트 스캔 어노테이션 사용 금지`() {
        noClasses()
            .should().beAnnotatedWith("org.springframework.stereotype.Component")
            .orShould().beAnnotatedWith("org.springframework.stereotype.Service")
            .orShould().beAnnotatedWith("org.springframework.stereotype.Repository")
            .orShould().beAnnotatedWith("org.springframework.context.annotation.ComponentScan")
            .orShould().beAnnotatedWith("org.springframework.boot.autoconfigure.SpringBootApplication")
            .check(classes)
    }

    @Test
    fun `service impl 클래스는 public이면 안 된다`() {
        val publicServiceImpls =
            classes
                .filter { it.simpleName.endsWith("ServiceImpl") }
                .filter { it.reflect().kotlin.visibility == KVisibility.PUBLIC }
                .map { it.name }

        assertThat(publicServiceImpls).isEmpty()
    }

    @Test
    fun `도메인 간 직접 의존 금지 — room은 template을 직접 참조하면 안 된다`() {
        noClasses()
            .that().resideInAPackage("com.naminhyeok.fantazzk.room..")
            .should().dependOnClassesThat().resideInAPackage("com.naminhyeok.fantazzk.template..")
            .check(classes)
    }

    @Test
    fun `도메인 간 직접 의존 금지 — template은 room을 직접 참조하면 안 된다`() {
        noClasses()
            .that().resideInAPackage("com.naminhyeok.fantazzk.template..")
            .should().dependOnClassesThat().resideInAPackage("com.naminhyeok.fantazzk.room..")
            .check(classes)
    }

    @Test
    fun `bootstrap은 room outport를 직접 참조하면 안 된다`() {
        noClasses()
            .that().resideInAPackage("com.naminhyeok.fantazzk.bootstrap..")
            .should().dependOnClassesThat().resideInAPackage("com.naminhyeok.fantazzk.room.outport..")
            .check(classes)
    }

    @Test
    fun `bootstrap은 template lookup service를 직접 참조하면 안 된다`() {
        noClasses()
            .that().resideInAPackage("com.naminhyeok.fantazzk.bootstrap..")
            .should().dependOnClassesThat().haveFullyQualifiedName("com.naminhyeok.fantazzk.template.TemplateLookupService")
            .check(classes)
    }
}
