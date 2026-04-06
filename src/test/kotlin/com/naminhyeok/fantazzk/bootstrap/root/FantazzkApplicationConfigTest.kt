package com.naminhyeok.fantazzk.bootstrap.root

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.yaml.snakeyaml.Yaml
import java.nio.file.Path
import kotlin.io.path.readText

class FantazzkApplicationConfigTest {
    @Test
    fun `애플리케이션은 PORT 환경 변수를 우선 사용한다`() {
        val defaultDocument = readYamlDocuments().first()

        assertThat(nestedValue(defaultDocument, "server", "port")).isEqualTo("\${PORT:8080}")
    }

    @Test
    fun `프로덕션 프로필은 actuator health만 노출한다`() {
        val productionDocument =
            readYamlDocuments()
                .first { nestedValue(it, "spring", "config", "activate", "on-profile") == "production" }

        val exposedEndpoints = nestedValue(productionDocument, "management", "endpoints", "web", "exposure", "include")

        assertThat(exposedEndpoints).isEqualTo(listOf("health"))
    }

    @Test
    fun `dev 프로필은 hikari maximum pool size를 5로 제한한다`() {
        val devDocument =
            readYamlDocuments()
                .first { nestedValue(it, "spring", "config", "activate", "on-profile") == "dev" }

        assertThat(nestedValue(devDocument, "spring", "datasource", "hikari", "maximum-pool-size")).isEqualTo(5)
    }

    @Test
    fun `dev 프로필은 hikari minimum idle을 0으로 유지한다`() {
        val devDocument =
            readYamlDocuments()
                .first { nestedValue(it, "spring", "config", "activate", "on-profile") == "dev" }

        assertThat(nestedValue(devDocument, "spring", "datasource", "hikari", "minimum-idle")).isEqualTo(0)
    }

    @Test
    fun `프로덕션 프로필은 hikari maximum pool size를 5로 제한한다`() {
        val productionDocument =
            readYamlDocuments()
                .first { nestedValue(it, "spring", "config", "activate", "on-profile") == "production" }

        assertThat(nestedValue(productionDocument, "spring", "datasource", "hikari", "maximum-pool-size")).isEqualTo(5)
    }

    @Test
    fun `프로덕션 프로필은 hikari minimum idle을 0으로 유지한다`() {
        val productionDocument =
            readYamlDocuments()
                .first { nestedValue(it, "spring", "config", "activate", "on-profile") == "production" }

        assertThat(nestedValue(productionDocument, "spring", "datasource", "hikari", "minimum-idle")).isEqualTo(0)
    }

    private fun readYamlDocuments(): List<Map<*, *>> =
        Yaml()
            .loadAll(Path.of("src/main/resources/application.yml").readText())
            .filterIsInstance<Map<*, *>>()
            .toList()

    private fun nestedValue(
        document: Map<*, *>,
        vararg keys: String,
    ): Any? {
        var current: Any? = document

        for (key in keys) {
            current = (current as? Map<*, *>)?.get(key) ?: return null
        }

        return current
    }
}
