package com.naminhyeok.fantazzk.architecture

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.SpringBootApplication
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readLines

class SpringModulithMigrationTest {
    @Test
    fun `루트 애플리케이션 엔트리포인트는 최상위 패키지에 위치한다`() {
        val applicationClass = Class.forName("com.naminhyeok.fantazzk.FantazzkApplication")

        assertThat(applicationClass.isAnnotationPresent(SpringBootApplication::class.java)).isTrue()
    }

    @Test
    fun `레거시 room 과 template 독립 애플리케이션은 제거되었다`() {
        assertThatThrownBy {
            Class.forName("com.naminhyeok.fantazzk.bootstrap.room.RoomApplication")
        }.isInstanceOf(ClassNotFoundException::class.java)

        assertThatThrownBy {
            Class.forName("com.naminhyeok.fantazzk.bootstrap.template.TemplateApplication")
        }.isInstanceOf(ClassNotFoundException::class.java)
    }

    @Test
    fun `모듈 내부 구현은 더 이상 api 나 query named interface 를 노출하지 않는다`() {
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
    fun `template 모듈은 명시적인 spi named interface 를 유지한다`() {
        assertThatCode {
            Class.forName("com.naminhyeok.fantazzk.template.spi.TemplateSpiPackageInfo")
        }.doesNotThrowAnyException()
    }

    @Test
    fun `dead room projection Liquibase 스키마와 cleanup 자산은 제거되었다`() {
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
    fun `dead template projection Liquibase 스키마와 cleanup 자산은 제거되었다`() {
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
    fun `소스 파일 경로는 패키지 선언과 일치한다`() {
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
