package com.naminhyeok.fantazzk.bootstrap.root

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@ActiveProfiles("test")
class FantazzkSmokeTest(
    private val restTemplate: TestRestTemplate,
) {
    @Test
    fun `Swagger UI가 접근 가능하다`() {
        restTemplate.getForEntity("/swagger-ui/index.html", String::class.java).also {
            assertThat(it.statusCode).isEqualTo(HttpStatus.OK)
        }
    }

    @Test
    fun `API 문서 엔드포인트가 응답한다`() {
        restTemplate.getForEntity("/v3/api-docs", String::class.java).also {
            assertThat(it.statusCode).isEqualTo(HttpStatus.OK)
        }
    }
}
