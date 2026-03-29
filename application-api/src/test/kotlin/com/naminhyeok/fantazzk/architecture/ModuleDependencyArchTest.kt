package com.naminhyeok.fantazzk.architecture

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.Test

class ModuleDependencyArchTest {
    // ArchUnit 1.4.0이 Java 25 class format을 지원하지 않아 importJars/importUrls가 빈 결과 반환.
    // Java 25 호환 버전 출시 시 importUrls 방식으로 전환하고 allowEmptyShould 제거할 것.
    private val classes: JavaClasses =
        ClassFileImporter()
            .withImportOption(ImportOption.DoNotIncludeTests())
            .importPackages("com.naminhyeok.fantazzk")

    @Test
    fun `컴포넌트 스캔 어노테이션 사용 금지`() {
        noClasses()
            .should().beAnnotatedWith("org.springframework.stereotype.Component")
            .orShould().beAnnotatedWith("org.springframework.stereotype.Service")
            .orShould().beAnnotatedWith("org.springframework.stereotype.Repository")
            .orShould().beAnnotatedWith("org.springframework.context.annotation.ComponentScan")
            .orShould().beAnnotatedWith("org.springframework.boot.autoconfigure.SpringBootApplication")
            .allowEmptyShould(true)
            .check(classes)
    }

    @Test
    fun `service impl 클래스는 public이면 안 된다`() {
        noClasses()
            .that().haveSimpleNameEndingWith("ServiceImpl")
            .should().bePublic()
            .allowEmptyShould(true)
            .check(classes)
    }

    @Test
    fun `도메인 간 직접 의존 금지 — room은 template을 직접 참조하면 안 된다`() {
        noClasses()
            .that().resideInAPackage("com.naminhyeok.fantazzk.room..")
            .should().dependOnClassesThat().resideInAPackage("com.naminhyeok.fantazzk.template..")
            .allowEmptyShould(true)
            .check(classes)
    }

    @Test
    fun `도메인 간 직접 의존 금지 — template은 room을 직접 참조하면 안 된다`() {
        noClasses()
            .that().resideInAPackage("com.naminhyeok.fantazzk.template..")
            .should().dependOnClassesThat().resideInAPackage("com.naminhyeok.fantazzk.room..")
            .allowEmptyShould(true)
            .check(classes)
    }
}
