package com.naminhyeok.fantazzk.architecture

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.Test

class ModuleDependencyArchTest {
    private val classes =
        ClassFileImporter()
            .withImportOption(ImportOption.DoNotIncludeTests())
            .importPackages("com.naminhyeok.fantazzk.teambuilding")

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
}
