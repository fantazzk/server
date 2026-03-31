package com.naminhyeok.fantazzk.template.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

class TemplatePlayerTest {
    @Test
    fun `템플릿 선수는 선언된 속성을 그대로 노출한다`() {
        val createdAt = Instant.parse("2025-01-01T00:00:00Z")
        val updatedAt = Instant.parse("2025-01-02T00:00:00Z")

        val player =
            TemplatePlayer(
                templatePlayerId = 3L,
                templateId = 7L,
                name = "선수1",
                displayOrder = 2,
                createdAt = createdAt,
                updatedAt = updatedAt,
            )

        assertThat(player.templatePlayerId).isEqualTo(3L)
        assertThat(player.templateId).isEqualTo(7L)
        assertThat(player.name).isEqualTo("선수1")
        assertThat(player.displayOrder).isEqualTo(2)
        assertThat(player.createdAt).isEqualTo(createdAt)
        assertThat(player.updatedAt).isEqualTo(updatedAt)
    }
}
