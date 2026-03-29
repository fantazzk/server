package com.naminhyeok.fantazzk.bootstrap.root

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.yaml.snakeyaml.Yaml

class FantazzkApplicationConfigTest {
    @Test
    fun `프로덕션 프로필은 actuator health만 노출한다`() {
        val productionDocument =
            readYamlDocuments()
                .first { nestedValue(it, "spring", "config", "activate", "on-profile") == "production" }

        val exposedEndpoints = nestedValue(productionDocument, "management", "endpoints", "web", "exposure", "include")

        assertThat(exposedEndpoints).isEqualTo(listOf("health"))
    }

    private fun readYamlDocuments(): List<Map<*, *>> =
        checkNotNull(javaClass.classLoader.getResourceAsStream("application.yml"))
            .use { inputStream ->
                Yaml()
                    .loadAll(inputStream)
                    .filterIsInstance<Map<*, *>>()
                    .toList()
            }

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
