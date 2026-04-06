package com.naminhyeok.fantazzk.template

import com.naminhyeok.fantazzk.template.domain.TemplatePlayer
import com.naminhyeok.fantazzk.template.domain.TemplatePlayerId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class TemplatePlayerTest {
    @Test
    fun `템플릿 선수는 선언된 속성을 그대로 노출한다`() {
        val createdAt = Instant.parse("2025-01-01T00:00:00Z")
        val updatedAt = Instant.parse("2025-01-02T00:00:00Z")
        val templatePlayerId = TemplatePlayerId.from(UUID.fromString("00000000-0000-0000-0000-000000000003"))
        val templateId = TemplateId.from("00000000-0000-0000-0000-000000000007")

        val player =
            TemplatePlayer(
                templatePlayerId,
                templateId,
                "선수1",
                2,
                createdAt,
                updatedAt,
            )

        assertThat(player.templatePlayerId).isEqualTo(templatePlayerId)
        assertThat(player.templateId).isEqualTo(templateId)
        assertThat(player.name).isEqualTo("선수1")
        assertThat(player.displayOrder).isEqualTo(2)
        assertThat(player.createdAt).isEqualTo(createdAt)
        assertThat(player.updatedAt).isEqualTo(updatedAt)
    }
}
