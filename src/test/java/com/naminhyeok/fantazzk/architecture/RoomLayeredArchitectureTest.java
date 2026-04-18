package com.naminhyeok.fantazzk.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

@AnalyzeClasses(packages = "com.naminhyeok.fantazzk", importOptions = ImportOption.DoNotIncludeTests.class)
class RoomLayeredArchitectureTest {
    private static final String ROOM_ROOT_PACKAGE = "com.naminhyeok.fantazzk.room";

    @ArchTest
    static final ArchRule room_layered_architecture = layeredArchitecture()
        .consideringOnlyDependenciesInLayers()
        .withOptionalLayers(true)
        .layer("Web").definedBy("com.naminhyeok.fantazzk.room.web..")
        .layer("Application").definedBy("com.naminhyeok.fantazzk.room.application..")
        .layer("Domain").definedBy("com.naminhyeok.fantazzk.room.domain..")
        .layer("Infrastructure").definedBy("com.naminhyeok.fantazzk.room.infrastructure..")
        .whereLayer("Web").mayOnlyAccessLayers("Application", "Domain")
        .whereLayer("Application").mayOnlyAccessLayers("Domain")
        .whereLayer("Domain").mayNotAccessAnyLayer()
        .whereLayer("Infrastructure").mayOnlyAccessLayers("Application", "Domain");

    @ArchTest
    static final ArchRule room_web_classes_may_only_depend_on_root_contracts = classes()
        .that().resideInAPackage("com.naminhyeok.fantazzk.room.web..")
        .should(onlyDependOnRoomRootContracts());

    @ArchTest
    static final ArchRule room_web_room_must_not_depend_on_room_web_game = noClasses()
        .that().resideInAPackage("com.naminhyeok.fantazzk.room.web.room..")
        .should().dependOnClassesThat().resideInAnyPackage("com.naminhyeok.fantazzk.room.web.game..");

    @ArchTest
    static final ArchRule room_web_game_must_not_depend_on_room_web_room = noClasses()
        .that().resideInAPackage("com.naminhyeok.fantazzk.room.web.game..")
        .should().dependOnClassesThat().resideInAnyPackage("com.naminhyeok.fantazzk.room.web.room..");

    @ArchTest
    static final ArchRule room_root_must_not_contain_controllers_requests_or_responses = classes()
        .that().resideInAPackage(ROOM_ROOT_PACKAGE)
        .and().areTopLevelClasses()
        .should(haveSimpleNameNotMatching(".*(ApiController|Request|Response)$"));

    @ArchTest
    static final ArchRule room_root_must_not_contain_infrastructure_implementations = classes()
        .that().resideInAPackage(ROOM_ROOT_PACKAGE)
        .and().areTopLevelClasses()
        .should(haveSimpleNameNotMatching(".*Publisher$|.*Scheduler$|.*JpaRepository$|Jpa.*Reader$|Uuid.*"));

    @ArchTest
    static final ArchRule no_classes_outside_room_or_architecture_should_depend_on_room_layers = noClasses()
        .that().resideOutsideOfPackages(
            "com.naminhyeok.fantazzk.room..",
            "com.naminhyeok.fantazzk.architecture.."
        )
        .should().dependOnClassesThat().resideInAnyPackage(
            "com.naminhyeok.fantazzk.room.web..",
            "com.naminhyeok.fantazzk.room.application..",
            "com.naminhyeok.fantazzk.room.domain..",
            "com.naminhyeok.fantazzk.room.infrastructure.."
        );

    @ArchTest
    static final ArchRule no_classes_outside_room_or_architecture_should_depend_on_room_root_contracts = classes()
        .that().resideOutsideOfPackages(
            "com.naminhyeok.fantazzk.room..",
            "com.naminhyeok.fantazzk.architecture.."
        )
        .should(notDependOnRoomRootContracts());

    @ArchTest
    static final ArchRule room_query_seams_must_not_depend_on_root_views = noClasses()
        .that().resideInAnyPackage(
            "com.naminhyeok.fantazzk.room.application.query..",
            "com.naminhyeok.fantazzk.room.infrastructure.persistence.."
        )
        .should().dependOnClassesThat().haveFullyQualifiedName("com.naminhyeok.fantazzk.room.JoinableRoomView");

    @ArchTest
    static final ArchRule room_domain_room_must_not_depend_on_live_game_models = noClasses()
        .that().resideInAPackage("com.naminhyeok.fantazzk.room.domain.room..")
        .should().dependOnClassesThat().haveFullyQualifiedName(
            "com.naminhyeok.fantazzk.room.domain.game.Game"
        )
        .orShould().dependOnClassesThat().haveFullyQualifiedName(
            "com.naminhyeok.fantazzk.room.domain.game.GameStatus"
        )
        .orShould().dependOnClassesThat().haveFullyQualifiedName(
            "com.naminhyeok.fantazzk.room.domain.game.GameParticipant"
        )
        .orShould().dependOnClassesThat().haveFullyQualifiedName(
            "com.naminhyeok.fantazzk.room.domain.game.GamePlayer"
        )
        .orShould().dependOnClassesThat().haveFullyQualifiedName(
            "com.naminhyeok.fantazzk.room.domain.game.AuctionGame"
        )
        .orShould().dependOnClassesThat().haveFullyQualifiedName(
            "com.naminhyeok.fantazzk.room.domain.game.DraftGame"
        )
        .orShould().dependOnClassesThat().haveFullyQualifiedName(
            "com.naminhyeok.fantazzk.room.domain.game.AuctionParticipant"
        )
        .orShould().dependOnClassesThat().haveFullyQualifiedName(
            "com.naminhyeok.fantazzk.room.domain.game.DraftParticipant"
        )
        .orShould().dependOnClassesThat().haveFullyQualifiedName(
            "com.naminhyeok.fantazzk.room.domain.game.AuctionBid"
        )
        .orShould().dependOnClassesThat().haveFullyQualifiedName(
            "com.naminhyeok.fantazzk.room.domain.game.AuctionSettlement"
        )
        .orShould().dependOnClassesThat().haveFullyQualifiedName(
            "com.naminhyeok.fantazzk.room.domain.game.AuctionOutcome"
        )
        .orShould().dependOnClassesThat().haveFullyQualifiedName(
            "com.naminhyeok.fantazzk.room.domain.game.RosterMember"
        )
        .orShould().dependOnClassesThat().haveFullyQualifiedName(
            "com.naminhyeok.fantazzk.room.domain.game.DraftProgress"
        )
        .orShould().dependOnClassesThat().haveFullyQualifiedName(
            "com.naminhyeok.fantazzk.room.domain.game.BidSequence"
        )
        .orShould().dependOnClassesThat().haveFullyQualifiedName(
            "com.naminhyeok.fantazzk.room.domain.game.GameFactory"
        );

    private static ArchCondition<JavaClass> haveSimpleNameNotMatching(String pattern) {
        return new ArchCondition<>("have a simple name not matching " + pattern) {
            @Override
            public void check(JavaClass item, ConditionEvents events) {
                boolean matches = item.getSimpleName().matches(pattern);
                events.add(matches
                    ? SimpleConditionEvent.violated(item, item.getSimpleName() + " matches " + pattern)
                    : SimpleConditionEvent.satisfied(item, item.getSimpleName() + " does not match " + pattern));
            }
        };
    }

    private static ArchCondition<JavaClass> onlyDependOnRoomRootContracts() {
        return new ArchCondition<>("depend on room root contracts only when accessing the room root package") {
            @Override
            public void check(JavaClass item, ConditionEvents events) {
                for (Dependency dependency : item.getDirectDependenciesFromSelf()) {
                    JavaClass target = dependency.getTargetClass();
                    if (!ROOM_ROOT_PACKAGE.equals(target.getPackageName())) {
                        continue;
                    }
                    if (isRoomRootContract(target)) {
                        continue;
                    }
                    events.add(SimpleConditionEvent.violated(
                        item,
                        "%s depends on non-contract root type %s".formatted(item.getName(), target.getName())
                    ));
                }
            }
        };
    }

    private static ArchCondition<JavaClass> notDependOnRoomRootContracts() {
        return new ArchCondition<>("not depend on room root contracts") {
            @Override
            public void check(JavaClass item, ConditionEvents events) {
                for (Dependency dependency : item.getDirectDependenciesFromSelf()) {
                    JavaClass target = dependency.getTargetClass();
                    if (!isRoomRootContract(target)) {
                        continue;
                    }
                    events.add(SimpleConditionEvent.violated(
                        item,
                        "%s depends on room root contract %s".formatted(item.getName(), target.getName())
                    ));
                }
            }
        };
    }

    private static boolean isRoomRootContract(JavaClass javaClass) {
        return ROOM_ROOT_PACKAGE.equals(javaClass.getPackageName()) &&
            javaClass.getModifiers().contains(JavaModifier.PUBLIC) &&
            javaClass.getSimpleName().matches(".*(Api|View)$");
    }
}
