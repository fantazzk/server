package com.naminhyeok.fantazzk.template

import com.naminhyeok.fantazzk.template.domain.TemplatePlayer
import com.naminhyeok.fantazzk.template.domain.TemplatePlayerId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

class TemplatePlayerTest {
    @Test
    fun `TemplatePlayerId는 UUID 문자열에서 생성된다`() {
        val id = TemplatePlayerId.of("00000000-0000-0000-0000-000000000003")

        assertThat(id.value.toString()).isEqualTo("00000000-0000-0000-0000-000000000003")
    }

    @Test
    fun `저장된 템플릿 선수는 강타입 식별자와 소속 템플릿을 노출한다`() {
        val createdAt = Instant.parse("2025-01-01T00:00:00Z")
        val updatedAt = Instant.parse("2025-01-02T00:00:00Z")
        val playerId = TemplatePlayerId.of("00000000-0000-0000-0000-000000000003")
        val templateId = TemplateId.of("00000000-0000-0000-0000-000000000007")

        val player =
            TemplatePlayer(
                templatePlayerId = playerId,
                templateId = templateId,
                name = "선수1",
                displayOrder = 2,
                createdAt = createdAt,
                updatedAt = updatedAt,
            )

        assertThat(player.id).isEqualTo(playerId)
        assertThat(player.templateId).isEqualTo(templateId)
        assertThat(player.name).isEqualTo("선수1")
        assertThat(player.displayOrder).isEqualTo(2)
        assertThat(player.createdAt).isEqualTo(createdAt)
        assertThat(player.updatedAt).isEqualTo(updatedAt)
    }

    @Test
    fun `새 템플릿 선수도 생성 시 식별자를 가진다`() {
        val templateId = TemplateId.of("00000000-0000-0000-0000-000000000007")

        val player =
            TemplatePlayer(
                templateId = templateId,
                name = "선수1",
                displayOrder = 2,
            )

        assertThat(player.id).isNotNull
        assertThat(player.templateId).isEqualTo(templateId)
    }
}
