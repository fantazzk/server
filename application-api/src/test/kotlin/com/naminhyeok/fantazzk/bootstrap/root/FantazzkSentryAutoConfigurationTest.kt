package com.naminhyeok.fantazzk.bootstrap.root

import io.sentry.spring.boot4.SentryAutoConfiguration
import io.sentry.spring.boot4.SentryProperties
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class FantazzkSentryAutoConfigurationTest {
    private val contextRunner =
        ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SentryAutoConfiguration::class.java))
            .withPropertyValues(
                "spring.application.name=fantazzk-server",
                "sentry.enabled=true",
                "sentry.dsn=https://examplePublicKey@o0.ingest.us.sentry.io/0",
                "sentry.environment=test",
            )

    @Test
    fun `부트 4용 센트리 자동 설정이 루트 센트리 속성을 바인딩한다`() {
        contextRunner.run { context ->
            assertThat(context).hasSingleBean(SentryProperties::class.java)

            context.getBean(SentryProperties::class.java).also { properties ->
                assertThat(properties.dsn).isEqualTo("https://examplePublicKey@o0.ingest.us.sentry.io/0")
                assertThat(properties.environment).isEqualTo("test")
                assertThat(properties.isEnabled).isTrue()
            }
        }
    }
}
