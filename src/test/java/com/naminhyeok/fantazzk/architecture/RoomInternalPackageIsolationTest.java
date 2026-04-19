package com.naminhyeok.fantazzk.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.naminhyeok.fantazzk")
class RoomInternalPackageIsolationTest {
    @ArchTest
    static final ArchRule room_내부_패키지는_room_모듈_밖에서_직접_의존하지_않는다 =
        noClasses()
            .that()
            .resideOutsideOfPackage("..room..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "..room.domain..",
                "..room.application..",
                "..room.query..",
                "..room.repository..",
                "..room.web..",
                "..room.infrastructure.."
            );

    @ArchTest
    static final ArchRule room_domain은_내부_상위_레이어에_의존하지_않는다 =
        noClasses()
            .that()
            .resideInAnyPackage("..room.domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "..room.application..",
                "..room.query..",
                "..room.repository..",
                "..room.web..",
                "..room.infrastructure.."
            );

    @ArchTest
    static final ArchRule room_query는_web이나_infrastructure에_의존하지_않는다 =
        noClasses()
            .that()
            .resideInAnyPackage("..room.query..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..room.web..", "..room.infrastructure..");

    @ArchTest
    static final ArchRule room_application은_web이나_infrastructure에_의존하지_않는다 =
        noClasses()
            .that()
            .resideInAnyPackage("..room.application..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..room.web..", "..room.infrastructure..");

    @ArchTest
    static final ArchRule room_repository는_domain_외_내부_레이어에_의존하지_않는다 =
        noClasses()
            .that()
            .resideInAnyPackage("..room.repository..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "..room.application..",
                "..room.query..",
                "..room.web..",
                "..room.infrastructure.."
            );

    @ArchTest
    static final ArchRule room_web은_repository나_infrastructure에_직접_의존하지_않는다 =
        noClasses()
            .that()
            .resideInAnyPackage("..room.web..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..room.repository..", "..room.infrastructure..");

    @ArchTest
    static final ArchRule room_infrastructure는_web에_의존하지_않는다 =
        noClasses()
            .that()
            .resideInAnyPackage("..room.infrastructure..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..room.web..");
}
