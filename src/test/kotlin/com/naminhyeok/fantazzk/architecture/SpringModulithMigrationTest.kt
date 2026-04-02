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
    fun `현재 목표 구조는 루트 Liquibase 마스터만 사용한다`() {
        val rootMasterPath = "classpath:/db/changelog/db.changelog-master.yaml"
        val mainApplication = Path.of("src/main/resources/application.yml").readText()
        val integrationApplication = Path.of("src/integrationTest/resources/application.yml").readText()

        assertThat(mainApplication).contains("change-log: $rootMasterPath")
        assertThat(integrationApplication).contains("change-log: $rootMasterPath")
        assertThat(integrationApplication).doesNotContain("classpath:/db/changelog/team-building/db.changelog-master.yaml")
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
    fun `현재 목표 구조는 초기 스키마와 감사 컬럼 changeSet 의 역할을 분리한다`() {
        val initialSchema = Path.of("src/main/resources/db/changelog/team-building/initial_schema.sql").readText()
        val auditColumns = Path.of("src/main/resources/db/changelog/team-building/add_audit_columns.sql").readText()
        val templateUpdatedAtInInitialSchema =
            """
            template
            (
                id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                name                  VARCHAR(255) NOT NULL,
                mode                  VARCHAR(20)  NOT NULL,
                team_count            INT          NOT NULL,
                team_size             INT          NOT NULL,
                budget                INT,
                draft_order_strategy  VARCHAR(20),
                created_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at
            """.trimIndent()
        val templatePlayerAuditColumnsInInitialSchema =
            """
            template_player
            (
                id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                template_id   BIGINT       NOT NULL,
                name          VARCHAR(255) NOT NULL,
                display_order INT          NOT NULL,
                created_at
            """.trimIndent()
        val roomPlayerAuditColumnsInInitialSchema =
            """
            room_player
            (
                id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                room_id       BIGINT       NOT NULL,
                name          VARCHAR(255) NOT NULL,
                status        VARCHAR(20)  NOT NULL,
                display_order INT          NOT NULL,
                created_at
            """.trimIndent()
        val roomTeamLeaderAuditColumnsInInitialSchema =
            """
            room_team_leader
            (
                id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                room_id          BIGINT       NOT NULL,
                team_leader_id   VARCHAR(36)  NOT NULL,
                nickname         VARCHAR(255) NOT NULL,
                remaining_budget INT,
                created_at
            """.trimIndent()
        val roomTeamMemberAuditColumnsInInitialSchema =
            """
            room_team_member
            (
                id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                room_id        BIGINT       NOT NULL,
                team_leader_id VARCHAR(36)  NOT NULL,
                player_name    VARCHAR(255) NOT NULL,
                assign_order   INT          NOT NULL,
                created_at
            """.trimIndent()
        val roomBidUpdatedAtInInitialSchema =
            """
            room_bid
            (
                id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                room_id        BIGINT      NOT NULL,
                round          INT         NOT NULL,
                team_leader_id VARCHAR(36) NOT NULL,
                amount         INT         NOT NULL,
                created_at     TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at
            """.trimIndent()

        assertThat(initialSchema).contains("created_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP")
        assertThat(initialSchema).doesNotContain(templateUpdatedAtInInitialSchema)
        assertThat(initialSchema).doesNotContain(templatePlayerAuditColumnsInInitialSchema)
        assertThat(initialSchema).doesNotContain(roomPlayerAuditColumnsInInitialSchema)
        assertThat(initialSchema).doesNotContain(roomTeamLeaderAuditColumnsInInitialSchema)
        assertThat(initialSchema).doesNotContain(roomTeamMemberAuditColumnsInInitialSchema)
        assertThat(initialSchema).doesNotContain(roomBidUpdatedAtInInitialSchema)
        assertThat(auditColumns).contains("ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;")
        assertThat(auditColumns).contains("ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,")
        assertThat(auditColumns).contains("-- template: updated_at 추가")
        assertThat(auditColumns).contains("-- template_player: created_at, updated_at 추가")
        assertThat(auditColumns).contains("-- room_player: created_at, updated_at 추가")
        assertThat(auditColumns).contains("-- room_team_leader: created_at, updated_at 추가")
        assertThat(auditColumns).contains("-- room_team_member: created_at, updated_at 추가")
        assertThat(auditColumns).contains("-- room_bid: updated_at 추가")
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
        val sourceRoot = Path.of("src/main/kotlin/com/naminhyeok/fantazzk")

        val mismatches =
            Files.walk(sourceRoot).use { paths ->
                paths
                    .filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".kt") }
                    .toList()
                    .mapNotNull { path ->
                        val packageName =
                            path.readLines()
                                .firstOrNull { it.startsWith("package ") }
                                ?.removePrefix("package ")
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

        assertThat(mismatches).isEmpty()
    }
}
