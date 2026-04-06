package com.naminhyeok.fantazzk.architecture

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.SpringBootApplication
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readLines
import kotlin.io.path.readText

class SpringModulithMigrationTest {
    @Test
    fun `현재 목표 구조는 루트 애플리케이션 진입점을 최상위 패키지에 둔다`() {
        val applicationClass = Class.forName("com.naminhyeok.fantazzk.FantazzkApplication")

        assertThat(applicationClass.isAnnotationPresent(SpringBootApplication::class.java)).isTrue()
    }

    @Test
    fun `현재 목표 구조는 방과 템플릿을 별도 애플리케이션으로 두지 않는다`() {
        assertThatThrownBy {
            Class.forName("com.naminhyeok.fantazzk.bootstrap.room.RoomApplication")
        }.isInstanceOf(ClassNotFoundException::class.java)

        assertThatThrownBy {
            Class.forName("com.naminhyeok.fantazzk.bootstrap.template.TemplateApplication")
        }.isInstanceOf(ClassNotFoundException::class.java)
    }

    @Test
    fun `현재 목표 구조는 모듈 내부 구현 surface 를 api 와 query 로 노출하지 않는다`() {
        assertThatThrownBy {
            Class.forName("com.naminhyeok.fantazzk.room.api.RoomApiPackageInfo")
        }.isInstanceOf(ClassNotFoundException::class.java)

        assertThatThrownBy {
            Class.forName("com.naminhyeok.fantazzk.room.query.RoomQueryPackageInfo")
        }.isInstanceOf(ClassNotFoundException::class.java)

        assertThatThrownBy {
            Class.forName("com.naminhyeok.fantazzk.template.api.TemplateApiPackageInfo")
        }.isInstanceOf(ClassNotFoundException::class.java)

        assertThatThrownBy {
            Class.forName("com.naminhyeok.fantazzk.template.query.TemplateQueryPackageInfo")
        }.isInstanceOf(ClassNotFoundException::class.java)
    }

    @Test
    fun `현재 목표 구조는 template 루트 계약만 공개한다`() {
        assertThatThrownBy {
            Class.forName("com.naminhyeok.fantazzk.template.spi.TemplateLookup")
        }.isInstanceOf(ClassNotFoundException::class.java)

        assertThatThrownBy {
            Class.forName("com.naminhyeok.fantazzk.template.spi.TemplateSpiPackageInfo")
        }.isInstanceOf(ClassNotFoundException::class.java)

        assertThatThrownBy {
            Class.forName("com.naminhyeok.fantazzk.template.infrastructure.spi.TemplateLookupAdapter")
        }.isInstanceOf(ClassNotFoundException::class.java)

        assertThatThrownBy {
            Class.forName("com.naminhyeok.fantazzk.template.infrastructure.spi.TemplateSpiConfiguration")
        }.isInstanceOf(ClassNotFoundException::class.java)

        assertThatCode {
            Class.forName("com.naminhyeok.fantazzk.template.TemplateCatalog")
        }.doesNotThrowAnyException()
    }

    @Test
    fun `현재 목표 구조는 소비자 없는 room 과 template 이벤트 타입을 유지하지 않는다`() {
        listOf(
            "com.naminhyeok.fantazzk.room.RoomCreated",
            "com.naminhyeok.fantazzk.room.RoomJoined",
            "com.naminhyeok.fantazzk.room.RoomStarted",
            "com.naminhyeok.fantazzk.room.AuctionSettled",
            "com.naminhyeok.fantazzk.room.DraftPickCompleted",
            "com.naminhyeok.fantazzk.room.RoomCompleted",
            "com.naminhyeok.fantazzk.room.LeaderSnapshot",
            "com.naminhyeok.fantazzk.template.TemplateCreated",
            "com.naminhyeok.fantazzk.template.TemplatePlayerCreated",
        ).forEach { className ->
            assertThatThrownBy { Class.forName(className) }
                .isInstanceOf(ClassNotFoundException::class.java)
        }
    }

    @Test
    fun `현재 목표 구조는 aggregate 내부 로컬 이벤트 큐를 유지하지 않는다`() {
        val roomSource = readMainSource("com.naminhyeok.fantazzk.room.domain", "Room")
        val templateSource = readMainSource("com.naminhyeok.fantazzk.template.domain", "Template")

        listOf("pendingEvents", "drainEvents(", "recordCreated(", "restorePendingEvents(", "registerEvent(", "registerEvents(")
            .forEach { marker ->
                assertThat(roomSource).doesNotContain(marker)
            }
        listOf("pendingEvents", "drainEvents(", "recordCreated(", "registerEvent(")
            .forEach { marker ->
                assertThat(templateSource).doesNotContain(marker)
            }
    }

    @Test
    fun `현재 목표 구조는 루트 Liquibase 마스터와 test 전용 profile override 를 사용한다`() {
        val rootMasterPath = "classpath:/db/changelog/db.changelog-master.yaml"
        val mainApplication = Path.of("src/main/resources/application.yml").readText()
        val testApplication = Path.of("src/test/resources/application-test.yml")
        val teamBuildingMaster = Path.of("src/main/resources/db/changelog/team-building/db.changelog-master.yaml").readText()

        assertThat(mainApplication).contains("change-log: $rootMasterPath")
        assertThat(mainApplication).contains("on-profile: test")
        assertThat(testApplication).exists()
        assertThat(testApplication.readText()).contains("change-log: $rootMasterPath")
        assertThat(testApplication.readText()).contains("url: jdbc:tc:postgresql:16:///test")
        assertThat(Path.of("src/test/resources/application.yml")).doesNotExist()
        assertThat(Path.of("src/integrationTest")).doesNotExist()
        assertThat(teamBuildingMaster).contains("db.changelog-add-audit-columns.yaml")
    }

    @Test
    fun `현재 목표 구조는 modulith JDBC 스키마 자동 초기화를 사용하지 않는다`() {
        val mainApplication = Path.of("src/main/resources/application.yml").readText()

        assertThat(mainApplication).doesNotContain("schema-initialization")
        assertThat(mainApplication).doesNotContain("spring.modulith.events.jdbc")
    }

    @Test
    fun `현재 목표 구조는 스프링 데이터 JDBC 설정과 엔티티 소스를 두지 않는다`() {
        listOf(
            "com.naminhyeok.fantazzk.RootCombinedJdbcConfiguration",
            "com.naminhyeok.fantazzk.room.config.RoomJdbcConfiguration",
            "com.naminhyeok.fantazzk.room.config.EnumConverters",
            "com.naminhyeok.fantazzk.template.config.TemplateJdbcConfiguration",
            "com.naminhyeok.fantazzk.template.config.EnumConverters",
            "com.naminhyeok.fantazzk.room.repository.RoomEntity",
            "com.naminhyeok.fantazzk.room.repository.RoomPlayerEntity",
            "com.naminhyeok.fantazzk.room.repository.RoomTeamLeaderEntity",
            "com.naminhyeok.fantazzk.room.repository.RoomTeamMemberEntity",
            "com.naminhyeok.fantazzk.room.repository.RoomBidEntity",
            "com.naminhyeok.fantazzk.room.repository.RoomIdAttributeConverter",
        ).forEach { className ->
            assertThatThrownBy { Class.forName(className) }
                .isInstanceOf(ClassNotFoundException::class.java)
        }

        listOf(
            "src/main/kotlin/com/naminhyeok/fantazzk/RootCombinedJdbcConfiguration.kt",
            "src/main/kotlin/com/naminhyeok/fantazzk/room/config/RoomJdbcConfiguration.kt",
            "src/main/kotlin/com/naminhyeok/fantazzk/room/config/EnumConverters.kt",
            "src/main/kotlin/com/naminhyeok/fantazzk/template/config/TemplateJdbcConfiguration.kt",
            "src/main/kotlin/com/naminhyeok/fantazzk/template/config/EnumConverters.kt",
            "src/main/kotlin/com/naminhyeok/fantazzk/room/repository/RoomEntity.kt",
            "src/main/kotlin/com/naminhyeok/fantazzk/room/repository/RoomPlayerEntity.kt",
            "src/main/kotlin/com/naminhyeok/fantazzk/room/repository/RoomTeamLeaderEntity.kt",
            "src/main/kotlin/com/naminhyeok/fantazzk/room/repository/RoomTeamMemberEntity.kt",
            "src/main/kotlin/com/naminhyeok/fantazzk/room/repository/RoomBidEntity.kt",
            "src/main/kotlin/com/naminhyeok/fantazzk/room/repository/RoomIdAttributeConverter.kt",
        ).map { Path.of(it) }.forEach { path ->
            assertThat(path.exists()).isFalse()
        }
    }

    @Test
    fun `현재 목표 구조는 UUID 기반 초기 스키마를 권위 원천으로 두고 감사 컬럼 changeSet 은 no-op 으로 유지한다`() {
        val initialSchema = Path.of("src/main/resources/db/changelog/team-building/initial_schema.sql").readText()
        val auditColumns = Path.of("src/main/resources/db/changelog/team-building/add_audit_columns.sql").readText()
        val nonBlankAuditLines = auditColumns.lineSequence().filter(String::isNotBlank).toList()

        assertThat(Regex("""\bUUID PRIMARY KEY\b""").findAll(initialSchema).count()).isEqualTo(7)
        assertThat(Regex("""\bcreated_at\b""").findAll(initialSchema).count()).isEqualTo(7)
        assertThat(Regex("""\bupdated_at\b""").findAll(initialSchema).count()).isEqualTo(7)
        assertThat(initialSchema).doesNotContain("BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY")
        assertThat(initialSchema).contains("CREATE TABLE template")
        assertThat(initialSchema).contains("CREATE TABLE room")

        assertThat(nonBlankAuditLines).isNotEmpty()
        assertThat(nonBlankAuditLines).allMatch { it.trim().startsWith("--") }
        assertThat(auditColumns).contains("Audit columns are part of initial_schema.sql on the clean-rewrite branch.")
        assertThat(auditColumns).contains("Keep this changeSet as a no-op")
        assertThat(auditColumns).doesNotContain("ALTER TABLE")
        assertThat(auditColumns).doesNotContain("ADD COLUMN")
        assertThat(auditColumns).doesNotContain("IF NOT EXISTS")
    }

    @Test
    fun `현재 목표 구조는 방 투영용 변경 이력 스키마와 정리 자산을 두지 않는다`() {
        val changelogRoot = Path.of("src/main/resources/db/changelog/team-building")
        val masterChangelog = changelogRoot.resolve("db.changelog-master.yaml").readLines()

        assertThat(masterChangelog)
            .noneMatch { it.contains("db.changelog-room-projection.yaml") }
        assertThat(masterChangelog)
            .noneMatch { it.contains("db.changelog-room-projection-cleanup.yaml") }
        assertThat(changelogRoot.resolve("db.changelog-room-projection.yaml")).doesNotExist()
        assertThat(changelogRoot.resolve("room_projection.sql")).doesNotExist()
        assertThat(changelogRoot.resolve("db.changelog-room-projection-cleanup.yaml")).doesNotExist()
        assertThat(changelogRoot.resolve("room_projection_cleanup.sql")).doesNotExist()
    }

    @Test
    fun `현재 목표 구조는 템플릿 투영용 변경 이력 스키마와 정리 자산을 두지 않는다`() {
        val changelogRoot = Path.of("src/main/resources/db/changelog/team-building")
        val masterChangelog = changelogRoot.resolve("db.changelog-master.yaml").readLines()

        assertThat(masterChangelog)
            .noneMatch { it.contains("db.changelog-template-projection.yaml") }
        assertThat(masterChangelog)
            .noneMatch { it.contains("db.changelog-template-projection-cleanup.yaml") }
        assertThat(changelogRoot.resolve("db.changelog-template-projection.yaml")).doesNotExist()
        assertThat(changelogRoot.resolve("template_projection.sql")).doesNotExist()
        assertThat(changelogRoot.resolve("db.changelog-template-projection-cleanup.yaml")).doesNotExist()
        assertThat(changelogRoot.resolve("template_projection_cleanup.sql")).doesNotExist()
    }

    @Test
    fun `현재 목표 구조는 소스 파일 경로와 패키지 선언을 일치시킨다`() {
        val mismatches =
            mainSourceRoots()
                .flatMap(::findPackageMismatches)

        assertThat(mismatches).isEmpty()
    }

    private fun readMainSource(
        packageName: String,
        simpleName: String,
    ): String =
        mainSourceRoots()
            .asSequence()
            .flatMap { sourceRoot ->
                val relativeParent =
                    packageName
                        .split('.')
                        .drop(3)
                        .fold(Path.of("")) { acc, segment -> acc.resolve(segment) }
                sequenceOf("kt", "java").map { extension ->
                    sourceRoot.resolve(relativeParent).resolve("$simpleName.$extension")
                }
            }.firstOrNull(Files::exists)
            ?.readText()
            ?: error("소스 파일을 찾을 수 없습니다: $packageName.$simpleName")

    private fun findPackageMismatches(sourceRoot: Path): List<String> =
        if (!sourceRoot.exists()) {
            emptyList()
        } else {
            Files.walk(sourceRoot).use { paths ->
                paths
                    .filter {
                        Files.isRegularFile(it) &&
                            (it.fileName.toString().endsWith(".kt") || it.fileName.toString().endsWith(".java"))
                    }
                    .toList()
                    .mapNotNull { path ->
                        val packageName =
                            path.readLines()
                                .firstOrNull { it.startsWith("package ") }
                                ?.removePrefix("package ")
                                ?.removeSuffix(";")
                                ?: return@mapNotNull null

                        val expectedParent =
                            packageName
                                .split('.')
                                .drop(3)
                                .fold(Path.of("")) { acc, segment -> acc.resolve(segment) }
                        val actualParent = sourceRoot.relativize(path).parent ?: Path.of("")

                        if (actualParent != expectedParent) {
                            "${sourceRoot.relativize(path)} -> $packageName"
                        } else {
                            null
                        }
                    }.toList()
            }
        }

    private fun mainSourceRoots(): List<Path> =
        listOf(
            Path.of("src/main/kotlin/com/naminhyeok/fantazzk"),
            Path.of("src/main/java/com/naminhyeok/fantazzk"),
        )
}
