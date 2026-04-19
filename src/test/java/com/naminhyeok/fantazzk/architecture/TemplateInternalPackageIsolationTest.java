package com.naminhyeok.fantazzk.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
    packages = "com.naminhyeok.fantazzk",
    importOptions = ImportOption.DoNotIncludeTests.class
)
class TemplateInternalPackageIsolationTest {
    @ArchTest
    static final ArchRule template_내부_패키지는_template_모듈_밖에서_직접_의존하지_않는다 =
        noClasses()
            .that()
            .resideOutsideOfPackage("..template..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "..template.domain..",
                "..template.application..",
                "..template.query..",
                "..template.repository..",
                "..template.web..",
                "..template.infrastructure.."
            );

    @ArchTest
    static final ArchRule template_domain은_내부_상위_레이어에_의존하지_않는다 =
        noClasses()
            .that()
            .resideInAnyPackage("..template.domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "..template.application..",
                "..template.query..",
                "..template.repository..",
                "..template.web..",
                "..template.infrastructure.."
            );

    @ArchTest
    static final ArchRule template_query는_web이나_infrastructure에_의존하지_않는다 =
        noClasses()
            .that()
            .resideInAnyPackage("..template.query..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..template.web..", "..template.infrastructure..");

    @ArchTest
    static final ArchRule template_application은_web이나_infrastructure에_의존하지_않는다 =
        noClasses()
            .that()
            .resideInAnyPackage("..template.application..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..template.web..", "..template.infrastructure..");

    @ArchTest
    static final ArchRule template_repository는_domain_외_내부_레이어에_의존하지_않는다 =
        noClasses()
            .that()
            .resideInAnyPackage("..template.repository..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "..template.application..",
                "..template.query..",
                "..template.web..",
                "..template.infrastructure.."
            );

    @ArchTest
    static final ArchRule template_web은_repository나_infrastructure에_직접_의존하지_않는다 =
        noClasses()
            .that()
            .resideInAnyPackage("..template.web..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..template.repository..", "..template.infrastructure..");

    @ArchTest
    static final ArchRule template_infrastructure는_web에_의존하지_않는다 =
        noClasses()
            .that()
            .resideInAnyPackage("..template.infrastructure..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..template.web..");
}
