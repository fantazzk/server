package com.naminhyeok.fantazzk.template

import com.naminhyeok.fantazzk.template.domain.TemplatePlayer
import com.naminhyeok.fantazzk.template.domain.TemplatePlayerId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Instant

class TemplatePlayerTest {
    @Test
    fun `저장된 템플릿 선수는 강타입 식별자와 소속 템플릿을 노출한다`() {
        val createdAt = Instant.parse("2025-01-01T00:00:00Z")
        val updatedAt = Instant.parse("2025-01-02T00:00:00Z")

        val player =
            TemplatePlayer(
                templatePlayerId = TemplatePlayerId(3L),
                templateId = TemplateId(7L),
                name = "선수1",
                displayOrder = 2,
                createdAt = createdAt,
                updatedAt = updatedAt,
            )

        assertThat(player.id).isEqualTo(TemplatePlayerId(3L))
        assertThat(player.templateId).isEqualTo(TemplateId(7L))
        assertThat(player.name).isEqualTo("선수1")
        assertThat(player.displayOrder).isEqualTo(2)
        assertThat(player.createdAt).isEqualTo(createdAt)
        assertThat(player.updatedAt).isEqualTo(updatedAt)
    }

    @Test
    fun `저장 전 템플릿 선수는 id 접근을 허용하지 않는다`() {
        val player =
            TemplatePlayer(
                templateId = TemplateId(7L),
                name = "선수1",
                displayOrder = 2,
            )

        assertThatThrownBy { player.id }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("TemplatePlayer id는 저장 후에만 사용할 수 있습니다")
    }
}
