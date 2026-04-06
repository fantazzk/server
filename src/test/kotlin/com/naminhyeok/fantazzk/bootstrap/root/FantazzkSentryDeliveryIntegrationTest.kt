package com.naminhyeok.fantazzk.bootstrap.root

import io.sentry.Sentry
import io.sentry.spring.boot4.SentryAutoConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import java.nio.file.Path
import java.util.UUID

@EnabledIfEnvironmentVariable(named = "SENTRY_TEST_DSN", matches = ".+")
class FantazzkSentryDeliveryIntegrationTest {
    @Test
    fun `설정된 프로젝트 dsn 으로 센트리 예외를 전송할 수 있다`() {
        val dsn = requireNotNull(System.getenv("SENTRY_TEST_DSN"))
        val marker = "fantazzk-sentry-smoke-${UUID.randomUUID()}"

        ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SentryAutoConfiguration::class.java))
            .withPropertyValues(
                "spring.application.name=fantazzk-server",
                "sentry.enabled=true",
                "sentry.dsn=$dsn",
                "sentry.environment=test",
            ).run {
                val eventId = Sentry.captureException(IllegalStateException(marker))
                Sentry.flush(5_000)
                println("SENTRY_SMOKE_MARKER=$marker")
                println("SENTRY_SMOKE_EVENT_ID=$eventId")
                System.getenv("SENTRY_SMOKE_OUTPUT")?.let { output ->
                    Path.of(output).toFile().writeText("marker=$marker\neventId=$eventId\n")
                }
                assertThat(eventId.toString()).isNotBlank()
                Sentry.close()
            }
    }
}
