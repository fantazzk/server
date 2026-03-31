package com.naminhyeok.fantazzk.architecture

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class InternalLayerArchitectureArchTest {
    private val classes: JavaClasses =
        ClassFileImporter()
            .withImportOption(ImportOption.DoNotIncludeTests())
            .importPackages("com.naminhyeok.fantazzk")

    @Test
    fun `room 내부 레이어 패키지가 모두 존재한다`() {
        assertThat(classes.containAnyInPackage("com.naminhyeok.fantazzk.room.model..")).isTrue()
        assertThat(classes.containAnyInPackage("com.naminhyeok.fantazzk.room.exception..")).isTrue()
        assertThat(classes.containAnyInPackage("com.naminhyeok.fantazzk.room.infrastructure..")).isTrue()
        assertThat(classes.containAnyInPackage("com.naminhyeok.fantazzk.room.service..")).isTrue()
        assertThat(classes.containAnyInPackage("com.naminhyeok.fantazzk.room.repository.jdbc..")).isTrue()
        assertThat(classes.containAnyInPackage("com.naminhyeok.fantazzk.room.web..")).isTrue()
    }

    @Test
    fun `template 내부 레이어 패키지가 모두 존재한다`() {
        assertThat(classes.containAnyInPackage("com.naminhyeok.fantazzk.template.model..")).isTrue()
        assertThat(classes.containAnyInPackage("com.naminhyeok.fantazzk.template.exception..")).isTrue()
        assertThat(classes.containAnyInPackage("com.naminhyeok.fantazzk.template.infrastructure..")).isTrue()
        assertThat(classes.containAnyInPackage("com.naminhyeok.fantazzk.template.service..")).isTrue()
        assertThat(classes.containAnyInPackage("com.naminhyeok.fantazzk.template.repository.jdbc..")).isTrue()
        assertThat(classes.containAnyInPackage("com.naminhyeok.fantazzk.template.api..")).isTrue()
        assertThat(classes.containAnyInPackage("com.naminhyeok.fantazzk.template.web..")).isTrue()
    }

    @Test
    fun `room model은 상위 레이어에 의존하면 안 된다`() {
        noClasses()
            .that().resideInAPackage("com.naminhyeok.fantazzk.room.model..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "com.naminhyeok.fantazzk.room.service..",
                "com.naminhyeok.fantazzk.room.web..",
                "com.naminhyeok.fantazzk.room.repository.jdbc..",
                "com.naminhyeok.fantazzk.room.infrastructure..",
            ).check(classes)
    }

    @Test
    fun `room infrastructure는 model 외 상위 레이어에 의존하면 안 된다`() {
        noClasses()
            .that().resideInAPackage("com.naminhyeok.fantazzk.room.infrastructure..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "com.naminhyeok.fantazzk.room.service..",
                "com.naminhyeok.fantazzk.room.web..",
                "com.naminhyeok.fantazzk.room.repository.jdbc..",
            ).check(classes)
    }

    @Test
    fun `room service는 api와 repository jdbc 구현에 의존하면 안 된다`() {
        noClasses()
            .that().resideInAPackage("com.naminhyeok.fantazzk.room.service..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "com.naminhyeok.fantazzk.room.web..",
                "com.naminhyeok.fantazzk.room.repository.jdbc..",
            ).check(classes)
    }

    @Test
    fun `room repository jdbc는 api와 service에 의존하면 안 된다`() {
        noClasses()
            .that().resideInAPackage("com.naminhyeok.fantazzk.room.repository.jdbc..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "com.naminhyeok.fantazzk.room.web..",
                "com.naminhyeok.fantazzk.room.service..",
            ).check(classes)
    }

    @Test
    fun `room api는 infrastructure와 repository jdbc 구현에 의존하면 안 된다`() {
        noClasses()
            .that().resideInAPackage("com.naminhyeok.fantazzk.room.web..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "com.naminhyeok.fantazzk.room.infrastructure..",
                "com.naminhyeok.fantazzk.room.repository.jdbc..",
            ).check(classes)
    }

    @Test
    fun `template model은 상위 레이어에 의존하면 안 된다`() {
        noClasses()
            .that().resideInAPackage("com.naminhyeok.fantazzk.template.model..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "com.naminhyeok.fantazzk.template.service..",
                "com.naminhyeok.fantazzk.template.api..",
                "com.naminhyeok.fantazzk.template.repository.jdbc..",
                "com.naminhyeok.fantazzk.template.infrastructure..",
                "com.naminhyeok.fantazzk.template.api..",
            ).check(classes)
    }

    @Test
    fun `template infrastructure는 model 외 상위 레이어에 의존하면 안 된다`() {
        noClasses()
            .that().resideInAPackage("com.naminhyeok.fantazzk.template.infrastructure..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "com.naminhyeok.fantazzk.template.service..",
                "com.naminhyeok.fantazzk.template.api..",
                "com.naminhyeok.fantazzk.template.repository.jdbc..",
                "com.naminhyeok.fantazzk.template.api..",
            ).check(classes)
    }

    @Test
    fun `template service는 web과 repository jdbc 구현에 의존하면 안 된다`() {
        noClasses()
            .that().resideInAPackage("com.naminhyeok.fantazzk.template.service..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "com.naminhyeok.fantazzk.template.web..",
                "com.naminhyeok.fantazzk.template.repository.jdbc..",
            ).check(classes)
    }

    @Test
    fun `template repository jdbc는 api와 service에 의존하면 안 된다`() {
        noClasses()
            .that().resideInAPackage("com.naminhyeok.fantazzk.template.repository.jdbc..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "com.naminhyeok.fantazzk.template.api..",
                "com.naminhyeok.fantazzk.template.service..",
            ).check(classes)
    }

    @Test
    fun `template api는 infrastructure와 repository jdbc 구현에 의존하면 안 된다`() {
        noClasses()
            .that().resideInAPackage("com.naminhyeok.fantazzk.template.api..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "com.naminhyeok.fantazzk.template.infrastructure..",
                "com.naminhyeok.fantazzk.template.repository.jdbc..",
                "com.naminhyeok.fantazzk.template.web..",
            ).check(classes)
    }

    private fun JavaClasses.containAnyInPackage(packageIdentifier: String): Boolean =
        any { javaClass -> javaClass.packageName.startsWith(packageIdentifier.removeSuffix("..")) }
}
