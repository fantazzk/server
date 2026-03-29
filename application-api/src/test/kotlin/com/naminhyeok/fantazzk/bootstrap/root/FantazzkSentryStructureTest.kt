package com.naminhyeok.fantazzk.bootstrap.root

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test

class FantazzkSentryStructureTest {
    @Test
    fun `스프링 부트 4용 센트리 자동 설정이 클래스패스에 존재한다`() {
        assertThatCode {
            Class.forName("io.sentry.spring.boot4.SentryAutoConfiguration")
        }.doesNotThrowAnyException()
    }

    @Test
    fun `루트 애플리케이션 설정이 프로필별 센트리 dsn 과 환경값을 선언한다`() {
        val yaml =
            FantazzkApplication::class.java.classLoader
                .getResourceAsStream("application.yml")
                ?.bufferedReader()
                ?.readText()

        assertThat(yaml)
            .isNotNull()
            .contains("sentry:")
            .contains("dsn: \${SENTRY_DSN:}")
            .contains("environment: dev")
            .contains("environment: production")
    }
}
