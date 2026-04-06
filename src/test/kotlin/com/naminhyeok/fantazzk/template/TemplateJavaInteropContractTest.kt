package com.naminhyeok.fantazzk.template

import com.naminhyeok.fantazzk.template.domain.TemplatePlayer
import com.naminhyeok.fantazzk.template.repository.Templates
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.lang.Nullable

class TemplateJavaInteropContractTest {
    @Test
    fun `repository lookup and template player nullable accessors declare nullability explicitly`() {
        assertThat(
            Templates::class.java.getMethod("findById", TemplateId::class.java).getAnnotation(Nullable::class.java),
        ).isNotNull()

        assertThat(
            TemplatePlayer::class.java.getMethod("getTemplatePlayerId").getAnnotation(Nullable::class.java),
        ).isNotNull()

        assertThat(
            TemplatePlayer::class.java.getMethod("getTemplateId").getAnnotation(Nullable::class.java),
        ).isNotNull()
    }
}
